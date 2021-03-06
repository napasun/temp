//
//  BannerImageView.swift
//  banner
//
//  Created by estgames on 2018. 1. 16..
//  Copyright © 2018년 estgames. All rights reserved.
//

import Foundation

//이미지 뷰
class BannerImageView: UIImageView {
    public var bannerEntry: Banners?
    
    required init?(coder aDecoder: NSCoder) {
        self.bannerEntry = nil
        super.init(coder: aDecoder)
    }
    
    init(_ entry: Banners, viewWidth: CGFloat, viewHeight: CGFloat, bottomViewHeight: CGFloat) {
        self.bannerEntry = entry
        
        super.init(frame: CGRect(x: 0, y: 0, width: viewWidth, height: viewHeight - bottomViewHeight))
        
        let tapGestureRecognizer = UITapGestureRecognizer(target: self
            , action: #selector(imageViewClick))
        
        let imgUrl = URL(string: entry.banner.content.resource)
        let dtinternet = try? Data(contentsOf: imgUrl!)
        if let itImg = dtinternet {
            self.image = UIImage(data: itImg)
            self.isUserInteractionEnabled = true
            self.addGestureRecognizer(tapGestureRecognizer)
        } else {
            //self.image = originalImage.imageWithRenderingMode(UIImageRenderingModeAlwaysTemplate)
            self.tintColor = UIColor.white
        }
    }
    
    @objc func imageViewClick() {
        switch self.bannerEntry!.banner.action.type {
        case "WEB_VIEW":    //TODO 이미지 뷰처리..
            if let url = URL(string: self.bannerEntry!.banner.action.url) {
                UIApplication.shared.open(url, options: [:])
            }
        case "WEB_BROWSER" :
            if let url = URL(string: self.bannerEntry!.banner.action.url) {
                UIApplication.shared.open(url, options: [:])
            }
        default:
            //NONE
            print("NONE")
            
        }
    }
}
