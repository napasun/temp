//
//  BannerLinkerButton.swift
//  estgames-common-framework
//
//  Created by estgames on 2018. 2. 14..
//

import Foundation

class LinkerButton: UIButton {
    let linkBtImage:UIImage
    let linkBtTitle:String = "자세히 보기"
    
    required init?(coder aDecoder: NSCoder) {
        linkBtImage = UIImage(named: "btn_detail_img", in:Bundle(for: CloseBt.self), compatibleWith:nil)!
        super.init(coder: aDecoder)
    }
    
    init() {
        linkBtImage = UIImage(named: "btn_detail_img", in:Bundle(for: CloseBt.self), compatibleWith:nil)!
        
        super.init(frame:CGRect.zero)
        self.setBackgroundImage(linkBtImage, for: .normal)
        self.titleLabel?.font = UIFont.boldSystemFont(ofSize: 13)
        self.titleLabel?.textColor = UIColor(red: 1, green: 1, blue: 1, alpha: 1.0)
        self.setTitle(linkBtTitle, for: .normal)
        self.addTarget(self, action: #selector(linkBtAction(_:)), for: .touchUpInside)
    }
    
    @objc func linkBtAction(_ sender:UIButton) {
        let entry:Entry = imageViews.last!.bannerEntry!
        
        switch entry.banner.action.type {
        case "WEB_VIEW":    //TODO 이미지 뷰처리..
            if let url = URL(string: entry.banner.action.url) {
                UIApplication.shared.open(url, options: [:])
            }
        case "WEB_BROWSER" :
            if let url = URL(string: entry.banner.action.url) {
                UIApplication.shared.open(url, options: [:])
            }
        default:
            //NONE
            print("NONE")
            
        }
    }
}
