//
//  EstTest.swift
//  objc_ex
//
//  Created by estgames on 2018. 5. 9..
//  Copyright © 2018년 estgames. All rights reserved.
//

import Foundation
import estgames_common_framework
import GoogleSignIn

@objc class EstTest : NSObject {
    //var est: EstgamesCommon
    
    @objc override init() {
        
    }
    
//    @objc init(view: UIViewController) {
//        est = EstgamesCommon(pview: view, initCallBack: {(ec:EstgamesCommon) -> Void in ec.authorityShow()})
//    }
//    @objc func banerShow () {
//        est.bannerShow()
//    }
    
    @objc func application_openURLWithOptionKey(_ app: UIApplication, open url: URL, options: [UIApplicationOpenURLOptionsKey : Any] = [:]) -> Bool {
        print("google: application")
        return GIDSignIn.sharedInstance().handle(url, sourceApplication: options[UIApplicationOpenURLOptionsKey.sourceApplication] as? String, annotation: options[UIApplicationOpenURLOptionsKey.annotation])
    }
}
