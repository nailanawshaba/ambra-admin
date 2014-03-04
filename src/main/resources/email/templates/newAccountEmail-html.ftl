<html>
<body>
Hello, <b>${displayName}</b>, a new account has been created for you.<br/>
<br/>
Please click on <a href="${url}?email=${email?url}&amp;verificationToken=${verificationToken?url}">this link</a> to
set your password:
<br/><br/>
or cut and paste the link below if you have problems:
<br/>
${url}?email=${email?url}&amp;verificationToken=${verificationToken?url}

</body>
</html>

