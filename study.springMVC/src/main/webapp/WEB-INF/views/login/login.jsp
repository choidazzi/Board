<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<script>
function fnLogin() {
	var frm = document.getElementById("frm");
	var username = document.getElementById("username");
	var password = document.getElementById("password");
	
	//id, pw validation ...
	
	return true;
}

function fn_signin() {
	location.href = "/signIn";
}
</script>

<form action="${CONTEXT_PATH}/login" method="post" id="frm" onsubmit="return fnLogin()">
	<input type="text" name="username" id="username"/>
	<input type="password" name="password" id="password"/>
	<button>로그인</button>
</form>
<button type="button" onclick="fn_signin()">회원가입</button>
