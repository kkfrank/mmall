<html>
<head>
    <meta charset="utf-8"/>
</head>
<body>
    <h2>Hello World!</h2>
        <form action="/manage/products/img-upload" method="post" enctype="multipart/form-data">
            <input type="file" name="upload_file">
            <input value="submit" type="submit">
        </form>

    <form action="/manage/products/rich-img-upload" method="post" enctype="multipart/form-data">
        <input type="file" name="upload_file">
        <input type="submit" value="富文本上传">
    </form>

    <form action="/users/login" method="post">
        <input type="text" name="username" placeholder="username">
        <input type="password" name="password" placeholder="password">
        <input type="submit" value="login">
    </form>

    <form action="/manage/users/login" method="post">
        <input type="text" name="username" placeholder="username">
        <input type="password" name="password" placeholder="password">
        <input type="submit" value="login">
    </form>

    <form action="/users/logout" method="post">
        <input type="submit" value="login">
    </form>
</body>
</html>
