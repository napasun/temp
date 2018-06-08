//
//  UserCloseButton.swift
//  estgames-common-framework
//
//  Created by estgames on 2018. 4. 5..
//

import Foundation

class UserCloseButton : UIButton {
    let closeButtonImage: UIImage?
    let view: UIViewController?
    var closeBtAction: () -> Void = {() -> Void in}
    
    init(_ pview: UIViewController, frame: CGRect) {
        closeButtonImage = UIImage(named: "btn_close_img_user", in:Bundle(for: UserCloseButton.self), compatibleWith:nil)
        view = pview
        super.init(frame: frame)
        
        if let cimg = closeButtonImage {
            self.setImage(cimg, for: .normal)
        } else {
            self.setTitle("X", for: .normal)
        }
        
        self.addTarget(self, action: #selector(closeBtAction(_:)), for: .touchUpInside)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc func closeBtAction(_ sender:UIButton) {
        view!.dismiss(animated: false, completion: closeBtAction)
    }
}
