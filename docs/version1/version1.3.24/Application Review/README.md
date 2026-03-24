# Application Review (Java SE Desktop/Swing)

纯 Java SE（无框架、无Servlet/Web）实现的 MO 端申请审核模块，采用 Swing 桌面多页面交互，基于 `../data` 目录真实数据运行。

## 运行环境

- JDK 8+
- Windows / Linux / macOS 均可（默认使用相对路径 `../data`）

## 启动方式

在 `Application Review` 目录执行：

```bash
javac -encoding UTF-8 -d out src/appreview/Main.java src/appreview/data/*.java src/appreview/model/*.java src/appreview/service/*.java src/appreview/ui/*.java src/appreview/util/*.java
java -cp out appreview.Main
```

可选指定数据目录：

```bash
java -cp out appreview.Main "..\\data"
```

## 界面类型

- Stand-alone Java Desktop Application（Swing，非 Servlet/Web）
- 单窗口多页面（CardLayout）切换：Dashboard / Applications / Detail / Review / Records

## 功能模块（对应原型5页）

1. MO Dashboard  
   - 顶部导航（Home/Job Management/Application Review/Logout）
   - 概览统计：Active Courses / Open Job Postings / Pending Reviews
   - 入口：Go to Job Management（占位）、Go to Application Review
2. TA Applications  
   - 统计：Total / Pending / Approved / Rejected
   - 搜索筛选：姓名/学号、课程、状态
   - 列表字段：TA Name、Student ID、Applied Course、Match Score、Missing Skills、Current Workload、Status
   - 操作：View Detail / Quick Approve / Quick Reject / Full Review
3. TA Application Detail  
   - 基础信息、技能匹配、工作量冲突、个人信息、经验背景、CV路径
   - 入口：Review Application / Review Now
4. Review TA Application  
   - 左右信息对比：课程要求 vs 申请人资质
   - 决策：Approve / Reject + Review Notes
   - Submit 写入数据并返回列表；Cancel 放弃
5. My Review Records  
   - 统计：Total / Approved / Rejected
   - 搜索筛选：关键词、时间范围（all/7/30）、结果
   - 查看详情：View（按 applicationId）
   - 导出：CSV / JSON / TXT 到 `export/`

## 数据读写说明

程序只使用 `version1.3.24/data` 数据：

- 读取
  - `data/applications/*.json`：申请数据
  - `data/jobs/*.json`：课程岗位数据
  - `data/users/ta/*.json`：TA信息
- 写入
  - `data/applications/*.json`：更新申请状态与审核信息
  - `data/logs/review_records.json`：审核记录（若不存在自动创建）

## 主要字段解释

- `applications/*.json`
  - `status.current`：申请状态（pending/approved/rejected）
  - `review.decisionReason`：审核备注
  - `review.reviewedAt`：审核时间
- `jobs/*.json`
  - `course.courseCode/courseName`：课程编码/名称
  - `content.preferredSkills`：匹配技能要求
  - `employment.weeklyHours`：岗位周工时
- `users/ta/*.json`
  - `profile`：TA基础信息
  - `academic.gpa`：GPA
  - `skills`：技能列表
  - `cv.filePath`：CV路径

## 目录结构

- `src/appreview/Main.java`：唯一入口
- `src/appreview/data`：JSON解析与文件仓储
- `src/appreview/model`：领域模型
- `src/appreview/service`：业务逻辑
- `src/appreview/ui/DesktopApp.java`：桌面多页面 UI
- `src/appreview/ui/ConsoleApp.java`：早期控制台版本（保留，不作为入口）
- `src/appreview/util`：控制台输出工具
- `export/`：审核记录导出目录（运行时自动创建）
- `doc/JavaDoc/`：JavaDoc 输出目录

## 异常处理

- 文件缺失、JSON格式异常、输入非法值均有友好提示；
- 程序不会因单次输入错误直接崩溃，可继续操作。
