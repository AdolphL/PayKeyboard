# PayKeyboard
Android 数字支付键盘
此项目只是个DEMO,目前有很多不足，但是也比较简单，可以down下来改改，根据需要各取所需吧。。。
## 效果图
![](https://github.com/AdolphL/picture/blob/master/paykeybroad/3.png)
![](https://github.com/AdolphL/picture/blob/master/paykeybroad/4.png)
## 使用方法
![](https://github.com/AdolphL/picture/blob/master/paykeybroad/2.png)
![](https://github.com/AdolphL/picture/blob/master/paykeybroad/1.png)
## Api
|方法名|参数|返回值|描述|
|:---|:---|:---|:---|
|clearPassword||void|清空当前已经输入的密码|
|setNumberLabel|String[]|void|为按键设置label, 格式类似{"1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "del"}|
|setInputListener|InputListener|void|设置6位密码输入后的回调函数,密码以object[6]位的形式返回|
|setEncryption|Encryption|void|设置密码加密类，此类会一个加密每一个用户所点击的Label值|
|setHelpMsgListener|ClickListener|void|设置点击forget pin一类的msg的回调函数|
|setTitleListener|ClickListener|void|设置点击头部关闭按钮的回调函数|
## 备注
暂无
