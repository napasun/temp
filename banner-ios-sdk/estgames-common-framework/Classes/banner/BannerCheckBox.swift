//
//  BannerCheckBox.swift
//  banner
//
//  Created by estgames on 2018. 1. 17..
//  Copyright © 2018년 estgames. All rights reserved.
//

import Foundation

//체크상자
class CheckBox: UIButton {
    var isChecked: Bool = false
    
    let checkImage:UIImage?
    let uncheckImage:UIImage?
    
    required init?(coder aDecoder: NSCoder) {
        checkImage = UIImage(named: "btn_stoptoday_check_img", in:Bundle(for: CheckBox.self), compatibleWith:nil)
        uncheckImage = UIImage(named: "btn_stoptoday_default_img", in:Bundle(for: CheckBox.self), compatibleWith:nil)
        super.init(coder: aDecoder)
    }

    init(leftMargin: CGFloat, topMargin: CGFloat, width: CGFloat, height: CGFloat, checkedCheckbox: String = "btn_stoptoday_check_img", uncheckedCheckbox: String = "btn_stoptoday_default_img") {
        checkImage = UIImage(named: checkedCheckbox, in:Bundle(for: CheckBox.self), compatibleWith:nil)
        uncheckImage = UIImage(named: uncheckedCheckbox, in:Bundle(for: CheckBox.self), compatibleWith:nil)
        
        super.init(frame: CGRect(x: leftMargin, y: topMargin, width: width, height: height))
    
        self.setBackgroundImage(uncheckImage, for: .normal)
        self.addTarget(self, action: #selector(checkBoxClick), for: .touchUpInside)
    }
    
    @objc func checkBoxClick() {
        isChecked = !isChecked
        if isChecked {
            self.setBackgroundImage(checkImage, for: .normal)
        } else {
            self.setBackgroundImage(uncheckImage, for: .normal)
        }
    }
    
    public func unCheckInit() {
        isChecked = false
        self.setBackgroundImage(uncheckImage, for: .normal)
    }
}