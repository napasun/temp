package com.estgames.estgames_framework.common

import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import com.amazonaws.mobile.auth.core.*
import com.amazonaws.mobile.auth.facebook.FacebookButton
import com.amazonaws.mobile.auth.google.GoogleButton
import com.amazonaws.mobile.auth.ui.AuthUIConfiguration
import com.estgames.aws.custom.EgAwsSignInActivity
import com.estgames.estgames_framework.common.ui.CustomProgressDialog
import com.estgames.estgames_framework.core.*
import com.estgames.estgames_framework.core.session.SessionManager
import com.estgames.estgames_framework.user.*
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import java.lang.Exception
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by mp on 2018. 5. 2..
 */
class UserService constructor(callingActivity: Activity) {
    var callingActivity = callingActivity
    var userAllDialog: UserAllDialog? = null

    /**
     * 콜백함수
     * */
    var failCallBack: CustomConsumer<Fail> = CustomConsumer {  }
    var startSuccessCallBack: Runnable = Runnable {  }
    var clearSuccessCallBack: Runnable = Runnable {  }
    var back:CustomConsumer<Activity> = CustomConsumer {  }

    var loginResultHandler: LoginResultHandler = object: LoginResultHandler {
            override fun onComplete(result: Result.Login) { }
            override fun onFail(code: Fail) { }
            override fun onCancel() { }
        }

    val sessionManager: SessionManager by lazy {
        SessionManager(callingActivity)
    }

    val identityManager: IdentityManager by lazy {
        IdentityManager.getDefaultIdentityManager()
    }

    private val preferences: ClientPreferences by lazy {
        ClientPreferences(callingActivity)
    }

    fun createUser() {
        identityManager.resumeSession(
                callingActivity,
                { result ->
                    if (sessionManager.hasSession) {
                        sessionManager.open()
                                .right { startSuccessCallBack.run() }
                                .left {
                                    if (it.code == Fail.TOKEN_INVALID || it.code == Fail.API_ACCESS_DENIED) {
                                        signout()
                                    }
                                    failCallBack.accept(it.code)
                                }
                    } else {
                        result.identityManager.getUserID(object : IdentityHandler {
                            override fun handleError(e: Exception?) {
                                failCallBack.accept(Fail.TOKEN_CREATION)
                            }

                            override fun onIdentityId(identityId: String?) {
                                sessionManager.create(principal=identityId!!)
                                        .right { startSuccessCallBack.run() }
                                        .left { failCallBack.accept(it.code)}
                            }
                        })
                    }
                },
                200
        )
    }

    fun goToLogin() {
        initLoginHandler()
        callingActivity.runOnUiThread(Runnable {
            val config = AuthUIConfiguration.Builder()
                    .signInButton(FacebookButton::class.java)
                    .signInButton(GoogleButton::class.java)
                    .userPools(false)
                    .build()
            EgAwsSignInActivity.startSignInActivity(callingActivity, config)
        })
    }

    fun goToLogin(config: AuthUIConfiguration) {
        initLoginHandler()
        EgAwsSignInActivity.startSignInActivity(callingActivity, config)
    }

    private fun retrieveEmail(provider: String): String? {
        return when (provider.toLowerCase()) {
            "facebook" -> Executors.newSingleThreadExecutor().use { executor ->
                val result = executor.submit(Callable {
                    return@Callable GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), null)
                            .apply {
                                parameters = Bundle().apply { putString("fields", "email, name, id") }
                            }
                            .executeAndWait()
                }).get().jsonObject

                return@use if (result.has("email")) result.getString("email") else null
            }
            "google" -> GoogleSignIn.getLastSignedInAccount(callingActivity)!!.email
            else -> null
        }
    }

    private inline fun <R> ExecutorService.use(code: (ExecutorService) -> R): R {
        try {
            return code(this)
        } finally {
            this.shutdown()
        }
    }

    private fun initLoginHandler() {
        val notYetGoogleSignIn = GoogleSignIn.getLastSignedInAccount(callingActivity) == null

        // Cognito 의 SNS 로그인 결과 Handler 등록.
        identityManager.login(callingActivity, object: DefaultSignInResultHandler() {
            override fun onSuccess(activity: Activity?, provider: IdentityProvider?) {
                callingActivity.runOnUiThread {
                    identityManager.getUserID(object: IdentityHandler{
                        override fun onIdentityId(identityId: String?) {
                            val email = retrieveEmail(provider!!.displayName)
                            val data = hashMapOf("provider" to provider.displayName)
                            if (email != null) data.put("email", email)

                            LoginSyncTask(
                                    callingActivity, identityId!!, data, provider,
                                    provider.run {
                                        return@run !"GOOGLE".equals(displayName.toUpperCase()) || notYetGoogleSignIn
                                    }
                            ).execute()
                        }

                        override fun handleError(exception: Exception?) {
                            Log.e("UserService", "SNS Login failed!!!", exception)
                            loginResultHandler.onFail(Fail.ACCOUNT_SYNC_FAIL)
                        }
                    })
                }
            }

            override fun onCancel(activity: Activity?): Boolean {
                back.accept(activity)
                return false
            }
        })
    }

    sealed class SyncState() {
        data class C(val egId: String, val userInfo: String): SyncState()
        data class E(val cause: EGException): SyncState()
    }

    inner class LoginSyncTask(
            activity: Activity,
            private val identity: String,
            private val data: Map<String, String>,
            private val provider: IdentityProvider,
            private val isNecessaryToLogout: Boolean):AsyncTask<Void, Void, Either<SyncState, Result.SyncComplete>>() {

        private val progress = CustomProgressDialog(activity, preferences)

        override fun onPreExecute() {
            progress.setMessage(com.estgames.estgames_framework.R.string.estcommon_user_sync_progress)
            progress.show()
        }

        override fun doInBackground(vararg p0: Void?): Either<SyncState, Result.SyncComplete> {
            return sessionManager.sync(data, identity).leftTo { err ->
                return@leftTo when (err) {
                    is Result.SyncFailure -> {
                        try {
                            val userInfo = GameAgent(callingActivity).retrieveGameUser(err.egId, preferences.locale.language);
                            SyncState.C(err.egId, userInfo)
                        } catch (e: EGException) {
                            SyncState.E(e)
                        }
                    }
                    else  -> {
                        val e = err as Result.Failure
                        if (e.cause is EGException) {
                            SyncState.E(e.cause)
                        } else {
                            SyncState.E(Fail.ACCOUNT_SYNC_FAIL.with(e.message))
                        }
                    }
                }
            }
        }

        override fun onPostExecute(result: Either<SyncState, Result.SyncComplete>?) {
            result!!.right {
                loginResultHandler.onComplete(Result.Login("LOGIN", it.egId, provider.displayName))
                progress.dismiss()
            }.left { state ->
                when (state) {
                    is SyncState.C -> {
                        // 계정 충돌이 발생했을 경우 충돌 처리 Dialog 창 오픈
                        callingActivity.runOnUiThread {
                            userAllDialog = UserAllDialog(callingActivity, preferences).apply {
                                setIdentityId(identity)
                                setProvider(provider.displayName)
                                setUserInfo(state.userInfo)
                                setData(data)
                                setOnCompleted { loginResultHandler.onComplete(it) }
                                setOnCancel {
                                    if (isNecessaryToLogout) {
                                        signout()
                                    }
                                    loginResultHandler.onCancel()
                                }
                                setOnFail {
                                    if (isNecessaryToLogout) {
                                        signout()
                                    }
                                    loginResultHandler.onFail(it.code)
                                }
                            }

                            userAllDialog!!.show()
                            progress.dismiss()
                        }
                    }
                    is SyncState.E -> {
                        signout()
                        loginResultHandler.onFail(state.cause.code)
                        progress.dismiss()
                    }
                }
            }
        }
    }

    fun signout() {
        if (identityManager.isUserSignedIn) {
            identityManager.signOut()
        }
    }

    fun logout() {
        if (identityManager.isUserSignedIn) {
            identityManager.signOut()
        }
        sessionManager.signOut()
        clearSuccessCallBack.run()
    }

    /**
     * Sync Dialog 핸들러 메서드.
     * 계정전환 버튼을 눌렀을 경우.
     */
    @Deprecated("Login Dialog 에 종속적인 코드를 포함하고 있어서 오류 가능성이 있음.")
    fun onSwitch() {
        sessionManager.create(identityManager.cachedUserID).right {
            //userResultDialog.show()
        }.left {e ->
            identityManager.signOut()
            failCallBack.accept(e.code)
            userAllDialog!!.dismiss()
        }
    }

    /**
     * Sync Dialog 핸들러 메서드.
     * 계정연동 버튼을 눌렀을 경우.
     */
    @Deprecated("Login Dialog 에 종속적인 코드를 포함하고 있어서 오류 가능성이 있음.")
    fun onSync() {
        val provider = identityManager.currentIdentityProvider.displayName
        sessionManager.sync(
                mapOf("provider" to provider), identityManager.cachedUserID, true
        ).right {
            //userResultDialog.show()
        }.left {
            val code: EGException = it as EGException
            identityManager.signOut()
            failCallBack.accept(code.code)
            userAllDialog!!.dismiss()
        }
    }
}