<?php
session_start();
if (isset($_POST['user_name']) && isset($_POST['password'])) {

	include "../DB_connection.php";

	function validate_input($data)
	{
		$data = trim($data);
		$data = stripslashes($data);
		$data = htmlspecialchars($data);
		return $data;
	}

	$user_name = validate_input($_POST['user_name']);
	$password  = $_POST['password']; // ✅ بدون htmlspecialchars

	if (empty($user_name)) {
		header("Location: ../login.php?error=User name is required");
		exit();
	}

	if (empty($password)) {
		header("Location: ../login.php?error=Password is required");
		exit();
	}

	$sql = "SELECT * FROM users WHERE username = ?";
	$stmt = $conn->prepare($sql);
	$stmt->execute([$user_name]);

	if ($stmt->rowCount() === 1) {
		$user = $stmt->fetch(PDO::FETCH_ASSOC);

		if (password_verify($password, $user['password'])) {

			$_SESSION['id'] = $user['id'];
			$_SESSION['role'] = $user['role'];
			$_SESSION['username'] = $user['username'];

			header("Location: ../index.php");
			exit();
		}
	}

	header("Location: ../login.php?error=Incorrect username or password");
	exit();
}
