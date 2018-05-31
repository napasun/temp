package com.estgames.estgames_framework.common

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.amazonaws.mobile.auth.core.DefaultSignInResultHandler
import com.amazonaws.mobile.auth.core.IdentityHandler
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.auth.core.IdentityProvider
import com.amazonaws.mobile.auth.facebook.FacebookButton
import com.amazonaws.mobile.auth.google.GoogleButton
import com.amazonaws.mobile.auth.ui.AuthUIConfiguration
import com.amazonaws.mobile.auth.ui.SignInActivity
import com.estgames.estgames_framework.core.Result
import com.estgames.estgames_framework.core.session.SessionManager
import com.estgames.estgames_framework.user.*
import java.lang.Exception

public class UserServiceTest  constructor(callingActivity: Activity) {
    var callingActivity = callingActivity

    /**
     *  팝업에 텍스트 설정하기
     * */
    public var setUserLinkMiddleText: CustormSupplier<String>? = null;
    public var setUserLinkBottomText: CustormSupplier<String>? = null;
    public var setUserLoadText: CustormSupplier<String>? = null;
    public var setUserGuestText: CustormSupplier<String>? = null;

    var userLinkDialog : UserLinkDialog = UserLinkDialog(callingActivity)
    var userLoadDialog : UserLoadDialog = UserLoadDialog(callingActivity)
    var userGuestLinkDialog : UserGuestLinkDialog = UserGuestLinkDialog(callingActivity)
    var userResultDialog : UserResultDialog = UserResultDialog(callingActivity)

    /**
     * 콜백함수
     * */


    public var startSuccessCallBack: Runnable = Runnable {  }
    public var startFailCallBack: CustomConsumer<String> = CustomConsumer {  }
    public var goToLoginSuccessCallBack: Runnable = Runnable {  }
    public var goToLoginFailCallBack: CustomConsumer<String> = CustomConsumer {  }
    public var goToLoginConfirmCallBack: Runnable = Runnable {  }
    public var clearSuccessCallBack: Runnable = Runnable {  }
    public var back:CustomConsumer<Activity> = CustomConsumer {  }


    val sessionManager: SessionManager by lazy {
        SessionManager(callingActivity)
    }

    val identityManager: IdentityManager by lazy {
        IdentityManager.getDefaultIdentityManager()
    }

    fun setting() {
        userLinkDialog = UserLinkDialog(callingActivity)
        userLinkDialog.confirmCallBack =  Runnable {
            userLoadDialog.show()
        }
        userLinkDialog.cancelCallBack = Runnable {
            userGuestLinkDialog.show()
        }
        userLinkDialog.closeCallBack = Runnable {
            Handler(Looper.getMainLooper()).post(Runnable {
                signout()
            })
        }

        if (setUserLinkMiddleText != null)
            userLinkDialog.userLinkSnsDataTextSupplier = setUserLinkMiddleText
        if (setUserLinkBottomText != null)
            userLinkDialog.userLinkGuestDataTextSupplier = setUserLinkBottomText

        userLoadDialog.confirmCallBack = Runnable {
            onSwitch()
        }
        userLoadDialog.closeCallBack = Runnable {
            userLoadDialog.dismiss()
            signout()
        }
        userLoadDialog.failConfirmCheck = goToLoginConfirmCallBack

        if (setUserLoadText != null)
            userLoadDialog.userLoadTextSupplier = setUserLoadText

        userGuestLinkDialog.loginCallBack = Runnable {
            onSync()
        }
        userGuestLinkDialog.beforeCallBack = Runnable {
            userLinkDialog.show()
        }
        userGuestLinkDialog.closeCallBack = Runnable {
            signout()
        }

        if (setUserGuestText != null)
            userGuestLinkDialog.userGuestTextSupplier = setUserGuestText

        userResultDialog.confirmCallBack = Runnable {
            goToLoginSuccessCallBack.run()
        }
        userResultDialog.closeCallBack = Runnable {
            goToLoginSuccessCallBack.run()
        }
    }

    val complete: (String) -> Unit = {t ->
        identityManager.login(callingActivity, object: DefaultSignInResultHandler() {
            override fun onSuccess(activity: Activity?, provider: IdentityProvider?) {
                identityManager.getUserID(object: IdentityHandler{
                    override fun onIdentityId(identityId: String?) {
                        sessionManager
                                .sync(hashMapOf("provider" to provider!!.displayName, "email" to "test@facebook.com"), identityId)
                                .right {
                                    //성공했을 경우
                                    goToLoginSuccessCallBack.run()
                                }
                                .left {
                                    //충돌이 발생했을 경우
                                    err ->
                                    when (err) {
                                        is Result.SyncFailure -> {
                                            // 계정 충돌이 발생했을 경우 충돌 처리 Dialog 창 오픈
                                            Handler(Looper.getMainLooper()).post(Runnable {
                                                setting()
                                                userLinkDialog.show()
                                            })
                                        }
                                        is Result.Failure -> {
                                            if (identityManager.isUserSignedIn) {
                                                identityManager.signOut()
                                            }
                                            goToLoginFailCallBack.accept(err.message)
                                        }
                                    }
                                }
                    }
                    override fun handleError(exception: Exception?) {
                        goToLoginFailCallBack.accept(exception.toString())
                    }
                })
            }

            override fun onCancel(activity: Activity?): Boolean {
                back.accept(activity!!)
                return false
            }
        })
        startSuccessCallBack.run()
    }

    val fail: (Throwable) -> Unit = {t ->
        startFailCallBack.accept(t.message)
    }

    public fun createUser() {
        identityManager.resumeSession(
                callingActivity,
                { result ->
                    if (sessionManager.hasSession) {
                        sessionManager.open().right(complete).left(fail)
                    } else {
                        result.identityManager.getUserID(object : IdentityHandler {
                            override fun handleError(e: Exception?) {
                                startFailCallBack.accept(e.toString())
                            }

                            override fun onIdentityId(identityId: String?) {
                                sessionManager.create(principal=identityId!!).right(complete).left(fail)
                            }
                        })
                    }
                },
                200
        )
    }

    public fun goToLogin() {
        callingActivity.runOnUiThread(Runnable {
            val config = AuthUIConfiguration.Builder()
                    .signInButton(FacebookButton::class.java)
                    .signInButton(GoogleButton::class.java)
                    .userPools(false)
                    .build()
            SignInActivity.startSignInActivity(callingActivity, config)
        })
    }

    public fun goToLogin(config: AuthUIConfiguration) {
        SignInActivity.startSignInActivity(callingActivity, config)
    }

    public fun signout() {
        if (identityManager.isUserSignedIn) {
            identityManager.signOut()
        }
    }

    public fun logout() {
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
    fun onSwitch() {
        sessionManager.create(identityManager.cachedUserID).right {
            userResultDialog.show()
        }.left {
            goToLoginFailCallBack.accept("sign in fail : $it")
            identityManager.signOut()
        }
    }

    /**
     * Sync Dialog 핸들러 메서드.
     * 계정연동 버튼을 눌렀을 경우.
     */
    fun onSync() {
        val provider = identityManager.currentIdentityProvider.displayName
        sessionManager.sync(
                mapOf("provider" to provider), identityManager.cachedUserID, true
        ).right {
            userResultDialog.show()
        }.left {
            goToLoginFailCallBack.accept("sync fail : $it")
            identityManager.signOut()
        }
    }
}