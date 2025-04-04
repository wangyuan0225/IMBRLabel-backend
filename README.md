## 前端项目
https://github.com/wangyuan0225/IMBRLabel-frontend

## 建表语句
```sql
create table annotation
(
    id           bigint auto_increment
        primary key,
    name         varchar(255) null comment '模板名',
    label        varchar(255) null comment '标签',
    stroke_style varchar(255) null comment '边线颜色',
    fill_style   varchar(255) null comment '填充颜色',
    line_width   int          null comment '边线宽度',
    user_id      bigint       null comment '用户id',
    create_time  datetime     null,
    update_time  datetime     null
);

create table image
(
    id          bigint auto_increment
        primary key,
    name        varchar(255) null comment '图片名称',
    type        varchar(7)   null comment '图片格式',
    path        varchar(255) null comment '图片路径',
    annotations longtext     null comment '标记文本',
    user_id     bigint       null comment '用户id',
    create_time datetime     null,
    update_time datetime     null
);

create table user
(
    id       bigint auto_increment
        primary key,
    username varchar(255) null comment '用户名',
    nickname varchar(255) null comment '用户昵称',
    password varchar(255) null comment '用户密码',
    email    varchar(255) null
)
    comment '用户表';
```

## **项目概述**

- **项目名称**：IMBRLabel
- **项目目标**：创建一个用户友好的图像标注工具，用于机器学习和计算机视觉项目中的数据准备。
- **目标用户**：数据科学家、机器学习工程师、研究人员和学生。

## **功能需求**

**2.1** **用户界面**

- **简洁直观的UI**：易于导航，可以是web或本地库

**2.2** **图像加载与管理**

**2.2.1** **图像格式支持**

- 支持的图像格式包括但不限于：JPEG, PNG,TIFF。
- 提供图像格式检测机制，确保只加载支持的格式。

**2.2.2** **图像预览**

- 在主界面提供缩略图浏览功能，用户可以快速浏览图像列表。
- 点击缩略图可以打开全尺寸图像进行标注。

**2.3** **标注工具**

**2.3.1** **基本形状标注**

- 提供矩形、圆形、椭圆形、直线、箭头等基本形状的标注工具。
- 每种形状工具都有相应的属性设置，如线条粗细、颜色等。

**2.3.2** **自定义形状标注**

- 允许用户通过鼠标绘制自定义多边形或不规则形状。
- 支持调整顶点位置，实现形状的自由变形。

**2.3.3** **标签系统**

- 用户可以创建和管理标签库，为不同类别的标注定义标签。

**2.3.4** **属性编辑**

- 标注完成后，用户可以为标注添加或编辑属性，如颜色、尺寸等。
- 提供属性模板，用户可以根据需要选择或自定义模板。

**2.4** **标注编辑**

**2.4.1** **标注操作**

- 标注创建后，用户可以移动、调整大小。

**2.4.2** **撤销/重做功能**

- 提供快捷键支持撤销和重做操作。

**2.5** **数据导出**

**2.5.1** **导出格式**

- 支持将标注数据导出为CSV、JSON、XML等格式。
- 每种格式都应包含图像路径、标注类型、位置、尺寸、标签和属性等信息。

**2.5.2** **导出选项**

- 提供导出设置，允许用户选择包含哪些信息，如仅包含标注数据或同时包含原始图像。

 
