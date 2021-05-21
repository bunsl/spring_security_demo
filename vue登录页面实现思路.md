# [Vue后台管理系统实现登录功能](https://www.cnblogs.com/home-/p/11581110.html)

登录页面

------

 

**![img](https://img2018.cnblogs.com/blog/1775427/201909/1775427-20190924205526580-888823969.png)**

 

 

 

 

***\*Vuex\*\*
\*\**\***

------

 

**![img](https://img2018.cnblogs.com/blog/1775427/201909/1775427-20190924205611892-1190847255.png)**

 

 

 

 

**router.js
**

------

 

**![img](https://img2018.cnblogs.com/blog/1775427/201909/1775427-20190924205640051-1958164113.png)**

 

 

**main.js设置全局**

------

 

**![img](https://img2018.cnblogs.com/blog/1775427/201909/1775427-20190924205717868-1823519518.png)**

 

 

 

------

------

**退出功能删除token 清空localStorage**

**![img](https://img2018.cnblogs.com/blog/1775427/201909/1775427-20190924210242635-1380882427.png)**

 

 

 

------

 

**显示用户名**

![img](https://img2018.cnblogs.com/blog/1775427/201909/1775427-20190924210337198-11112874.png)

 

 ![img](https://img2018.cnblogs.com/blog/1775427/201909/1775427-20190924210358035-1361300055.png)

 

 ![img](https://img2018.cnblogs.com/blog/1775427/201909/1775427-20190924210413702-675008646.png)

------

 

**键盘事件**

**![img](https://img2018.cnblogs.com/blog/1775427/201909/1775427-20190924210541709-775248670.png)**

 

 

 

------

 

 

 **记住密码操作( 读存取cookie与调后端数据无关 提交按钮中操作 )**

**![img](https://img2018.cnblogs.com/blog/1775427/201909/1775427-20190924210715609-566008050.png)**

 

 

 **点击事件中**

 

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```js
// 登录调接口
    handleSubmit(name) {
      // 判断单选框是否被选中
      if (this.single == true) {
        console.log("checked == true");
        //传入账号名，密码，和保存天数7天
        this.setCookie(this.formInline.account, this.formInline.password, 7);
      } else {
        console.log("清空Cookie");
        //清空Cookie
        this.clearCookie();
      }
      if (!this.formInline.account && !this.formInline.password) {
        this.$Message.error("请输入用户名和密码");
      } else if (!this.formInline.account) {
        this.$Message.error("请输入用户名");
      } else if (!this.formInline.password) {
        this.$Message.error("请输入密码");
      } else {
        this.$refs[name].validate(valid => {
          let val = {
            account: this.formInline.account,
            password: this.formInline.password
          };
          if (valid) {
            this.$ajax.post("/api/login", val).then(res => {
              if (res.data.code == 200) {
                this.initUserInfo();
                this.$Message.success({
                  content: "登陆成功",
                  duration: 0.3
                });
              } else {
                this.$Message.error("用户名不存在，或密码错误");
              }
            });
          } else {
            return false;
          }
        });
      }
    },
    //获取用户信息
    initUserInfo() {
      this.$ajax.get("/api/getUserInfo").then(res => {
        const userInfo = res.data.data;
        // // this.form.username = userInfo.account;
        localStorage.setItem("userInfo", this.formInline.account);
        this.$store.commit("changeUserInfo", JSON.stringify(userInfo));
        this.$router.push({ path: "/tabbar" });
        this.$Message.success({
          content: "登陆成功",
          duration: 0.3
        });
      });
    },
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

 

**设置cookie**

 

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```js
//设置cookie
    setCookie(c_name, c_pwd, exdays) {
      var exdate = new Date(); //获取时间
      exdate.setTime(exdate.getTime() + 24 * 60 * 60 * 1000 * exdays); //保存的天数
      //字符串拼接cookie
      window.document.cookie =
        "userName" + "=" + c_name + ";path=/;expires=" + exdate.toGMTString();
      window.document.cookie =
        "userPwd" + "=" + c_pwd + ";path=/;expires=" + exdate.toGMTString();
    },
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

 

**读取cookie**

 

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```js
//读取cookie
    getCookie: function() {
      if (document.cookie.length > 0) {
        var arr = document.cookie.split("; "); //这里显示的格式需要切割一下自己可输出看下
        for (var i = 0; i < arr.length; i++) {
          var arr2 = arr[i].split("="); //再次切割
          //判断查找相对应的值
          if (arr2[0] == "userName") {
            this.formInline.account = arr2[1]; //保存到保存数据的地方 用户名
          } else if (arr2[0] == "userPwd") {
            this.formInline.password = arr2[1]; //保存到保存数据的地方 密码
          }
        }
      }
    },
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

 

**清除cookie**

 

```js
//清除cookie
    clearCookie: function() {
      this.setCookie("", "", -1); //修改2值都为空，天数为负1天就好了
    }
```

 

**mounted中**初始化页面完成后，再对html的dom节点进行一些需要的操作。

 

```js
mounted() {
    this.getCookie();//调用
  }
```