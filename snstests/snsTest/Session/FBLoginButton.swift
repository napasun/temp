//
//  FBLoginButton.swift
//  snsTest
//
//  Created by estgames on 2018. 9. 18..
//  Copyright © 2018년 estgames. All rights reserved.
//

import Foundation
import FBSDKLoginKit

class FBLoginButton : FBSDKLoginButton {
    var manager:SessionManager = SessionManager()
    var view:UIViewController
    init(view : UIViewController) {
        self.view = view
        super.init(frame : CGRect.zero)
        
        self.removeTarget(self, action: #selector(signInAction(_:)), for: .touchUpInside)
        self.addTarget(self, action: #selector(signInAction(_:)), for: .touchUpInside)
    }
    @objc func signInAction(_ sender:UIButton) {
        var t:FBSDKLoginManager = FBSDKLoginManager()
        t.logIn(withReadPermissions: ["public_profile","email"], from: view, handler: { (result, error) in
            if error != nil {
                // handle error
                print(error)
            }
            var current = FBSDKAccessToken.current()

            print(current!.userID)
            
            FBSDKGraphRequest(graphPath: "me", parameters: ["fields": "email"])!.start(completionHandler: { (connection, result, error) -> Void in
                if (error == nil){
                    let fields = result as? [String:Any]
                    var data:[String:String] = [String:String]()
                    
                    data["provider"] = "facebook"
                    data["email"] = fields!["email"] as! String
                    self.manager.sync(principal: "facebook:\(current!.userID)", data: data)
                }
            })
        })
    }
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
