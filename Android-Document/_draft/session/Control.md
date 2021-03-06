# 로그인 컨트롤

`SignInControl` 클래스는 외부 프로바이더 계정의 로그인 과 EG 플랫폼 계정의 연동을 담당합니다. `SignInControl` 클래스를 통해 SNS 계정 연동을 간편하게 진행 할 수 있습니다.

> 게스트 계정의 로그인은 [`SessionManager`](/_draft/session/Open.md)를 이용하시면 됩니다.

## 컨트롤 객체 생성하기

`SignInControl` 은 factory 메소드를 통해서만 생성 할 수 있습니다. 컨트롤 객체를 생성하기 위해서는 컨트롤 객체에서 필요로 하는 값을 설정 해야 합니다. 값의 설정은 `Option` 객체를 사용합니다.

### _SignInControl.createControl_

* Parameters
  * _context_ : 안드로이드 Context
  * _option_ : 로그인 컨트롤 옵션

* Regurn _`SignInControl`_ : 로그인 컨트롤 객체

```java
SignInControl control = SignInControl.createControl(context, option);
```

## Option

`SignInControl` 객체를 생성을 위한 옵션을 설정 합니다.
옵션설정 값은 다음과 같습니다.

### Mode

* 데이터 타입 : _`String`_
* 값 유형 : sync (default), switch

외부 프로바이더 인증 후 연동 방법을 설정합니다. 값은 `sync`, `switch` 입니다. `switch`모드는 계정을 변경하여 세션을 생성합니다. 기본값은 `sync` 이며 대소문자를 가리지 않습니다.

### SignButton

* `SignButton`

로그인 버튼을 Control 객체와 연결합니다. Control 객체와 연결된 버튼은 따로 `OnClickHandler`를 등록하지 않아도 됩니다.

### SyncAware

* `AccountSyncAware` (인터페이스)

계정 연동시 계정 충돌 이벤트 핸들러를 등록합니다. 계정 충돌이 발생한 경우 처리를 위해 사용합니다. 따로 등록하지 않는 경우 SDK 기본핸들러가 등록됩니다.

### ResultHandler

* `SignInResultHandler` (인터페이스)

로그인 결과 핸들러를 등록합니다.

```java
    SignInControl.Option option = new SignInControl.option()
            .setMode("sync")          // 프로바이더 계정 연계 모드. 기본값은 SYNC (연동) 모드입니다.
            .addSignButton( ... )     // 로그인 버튼 등록.
            .setAccountSyncAware(new AccountSyncAware() {
                // 로그인 후 계정연결시 충돌상황에 대한 핸들러 등록
            })
            .setSignInResultHandler(new SignInResultHandler() {
                // 로그인 결과 핸들러 등록
            });
```

## SignInControl

`SignInControl` 클래스는 다음과 같은 메소드들을 포함 하고 있습니다.

### _isSignIn(Provider provider)_

* Parameters
  * provider : 로그인을 확인할 프로바이더
* Return _`boolean`_ : 프로바이더 로그인 여부

파라미터로 받은 프로바이더 계정의 로그인 여부를 확인합니다.

```java
boolean isSignedGoogle = signControl.isSignIn(Provider.GOOGLE);
```

### _signIn(Provider provider, Activity activity)_

* Parameters
  * provider : 로그인할 프로바이더
  * activity : 로그인을 실행하는 액티비티

파라미터로 받은 프로바이더 계정으로 로그인을 합니다. `SignInControl` 객체에 버튼을 연결하지 않고 버튼을 직접 구현하는 경우 사용합니다.

```java
signControl.signIn(Provider.GOOGLE, activity);
```

### _signIn(Activity activity)_

* Parameters
  * activity : 로그인을 실행하는 액티비티

지정 프로바이더 없이 로그인을 실행하는 경우 SDK에서 기본적으로 제공하는 로그인 팝업창을 호출합니다.

```java
signControl.signIn(activity);
```

> 로그인 프로세스는 어떤 형태이든 사용자의 응답을 받아야 합니다. 따라서 사용자의 응답을 처리하는 콜백을 등록해야 합니다. 이 역할을 하는것이 `onActivityResult()` 메서드입니다.
반드시 로그인을 실행하는 Activity의 ___onActivityResult___ 메서드를 오버라이드 하기 바랍니다.

### _onActivityResult(int requestCode, int resultCode, Intent data)_

* Parameters
  * requestCode : 로그인 요청 코드
  * responseCode : 로그인 결과 코드
  * data : 로그인 결과 데이터

로그인 결과 처리를 위한 콜백들을 호출합니다. 로그인 처리를 위해 반드시 로그인을 실행한  `Activity`의 `onActivityResult` 메소드에서 호출 해줘야 합니다.

```java
@Override
public void onActivityResult(int requestCode, int responseCode, Intent data) {
    ...
    signControl.onActivityResult(requestCode, responseCode, data);
}
```

## AccountSyncAware

계정 연동중 계정 충돌 이벤트를 제어하기 위한 핸들러 인터페이스입니다. 계정 충돌이 발생한경우 연동을 계송 진행할지 중단 할지 여부를 선택 할 수 있습니다.

`AccountSyncAware` 인터페이스의 메소드는 다음과 같습니다.

### _preProgress()_

계정 연동을 하기 직전에 호출됩니다. 연동중 화면 보호를 위한 스크린이나 ProgressBar를 보여주는 용도등으로 사용할 수 있습니다. Hook 메소드이므로 반드시 구현할 필요는 없습니다.

### _postProgress()_

계정 연동이 완료되거나 실패한 직후에 호출 됩니다. 연동 직전에 보여준 ProgressBar를 회수하는 용도등으로 사용할 수 있습니다. Hook 메소드이므로 반드시 구현할 필요는 없습니다.

### _onMismatched(Mismatch mismatch, Step next)_

계정 연동중 계정 충돌이 발생한 경우에 호출됩니다. 충돌한 계정 정보와 다음 프로세스의 진행 여부를 선택 할 수 있습니다.
계정 연동중 계정 충돌이 발생한 경우 `onMismatched` 메소드가 호출됩니다. 이때 SDK 는 `Mismatch` 객체와 `Step` 객체를 넘겨주게 됩니다.

## Mismatch

충돌이 발생한 계정 정보를 포함하고 있습니다.

|속성 이름|데이터 타입|설명|
|-|-|-|
|conflictId|String|충돌이 발생한 EG ID 값입니다. 현재 세션의 EG ID 값이 아닙니다.|
|user|String|Provider 계정의 사용자 정보입니다. 구글과 페이스북의 경우 사용자 Email 계정입니다.|
|data|Map|Provider 계정의 상세 정보입니다. key - value 형태의 값으로 넘겨줍니다.|

## Step

다음 프로세스의 처리 여부를 SDK에 전달합니다.

|메소드 이름|파라미터|설명|
|-|-|-|
|stop|-|계정 연동을 중단합니다. 이 경우 ResultHandler 에 취소로 값을 전달합니다.|
|stop|Throwable|계정 연동을 중단합니다. 이경우 ResultHandler 에 에러로 값을 전달합니다.|
|proceed|-|충돌이 발생했으나 강제로 계정 연동을 진행합니다. 충돌한 계정의 정보는 삭제됩니다. 이 메소드는 비동기로 동작합니다.|
|proceedAndAwait|-|충돌이 발생했으나 강제로 계정연동을 진행합니다. 충돌한 계정의 정보는 삭제됩니다. 이 메소드는 동기모드로 동작합니다. 이 후에 반드시 `done()` 메소드를 호출 해야 합니다.|
|done|-|모든 프로세스를 완료합니다. 이 메소드는 ResultHandler 에 값을 전달합니다. 이 메소드를 호출 하면 동기모드로 진행 할 경우에만 호출 해 주면 됩니다.|

`proceed()` 메소드의 경우 동기와 비동기모드로 선택하여 진행 할 수 있습니다. 비동기 모드로 진행 할 경우 모든 프로세스가 완료되면 자동으로 ResultHandler에 값을 전달합니다. 그러나 동기 모드로 진행할 경우 `done()` 메소드를 호출하여 프로세스 완료 처리를 해주어야 합니다. `stop()` 의 경우에는 중단과 동시에 완료처리를 합니다.

## SignInResultHandler

로그인 결과를 전달 받는 핸들러 인터페이스입니다. Provider 계정 연동 후 결과를 처리하기 위해 사용합니다.

`SignInResultHandler` 인터페이스의 메소드는 다음과 같습니다.

### _onComplete(Result.Login result)_

계정연동이 성공한 경우 호출됩니다.

### _onError(Throwable t)_

계정연동이 실패한 경우 호출됩니다. 실패한 예외 객체를 넘겨받습니다.

### _onCancel()_

계정연동을 취소한 경우 호출됩니다.

## Result.Login

성공한 계정 연동 정보를 포함하는 데이터 객체입니다.

|속성이름|데이터 타입|설명|
|-|-|-|
|type|String|계정연동 유형입니다. LOGIN, SYNC 값으로 어떤 방식으로 로그인 했는지 판단합니다.|
|egId|String|계정을 연동한 후 EG ID. 현재 세션의 EG ID 와 동일합니다.|
|provider|String|계정 연동을 진행한 프로바이더 값입니다.|