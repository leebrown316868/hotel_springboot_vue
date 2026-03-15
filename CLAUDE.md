# 酒店民宿管理系统

## 项目注意事项
- 启动项目或重启项目必须结束所有java进程taskkill //F //IM java.exe，然后mvn spring-boot:run，run mvn spring-boot:run must focus only on errors
- 启动前端项目或重启前端项目必须先结束所有的前端node进程taskkill //F //IM bash.exe，然后npm run dev
- 中文输出
- win11环境
## 调试
- 简单问题简单解决，不要过度复杂化
- 先看错误信息，再进行调试
- 清除缓存是解决前端问题的有效方法
- Firefox的调试输出比Chrome更清晰
## claude需要注意
- 每次成功调试即必须接收到用户回复ok后才能将本次问题和答案总结为一句话并加入"E:\project\hotel\问题.md"，方便后续调试
- 每次claude的Bash小任务失败，但下一个小任务成功解决时，就记住这个解决方法到CLAUDE.md,避免未来犯同样错误