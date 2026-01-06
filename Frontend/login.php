<?php
session_start();

$error = "";
$debugInfo = "";

if ($_SERVER["REQUEST_METHOD"] === "POST") {
    $username = trim($_POST['username'] ?? '');
    $password = trim($_POST['password'] ?? '');

    // Debug info
    $debugInfo = "Username: '$username', Password length: " . strlen($password);

    // Call Java backend API for authentication
    $apiUrl = "http://localhost:8082/English-darija-tr/rest/auth/login";
    
    $data = array(
        "username" => $username,
        "password" => $password
    );
    
    $ch = curl_init($apiUrl);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, array(
        'Content-Type: application/json',
        'Accept: application/json'
    ));
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
    curl_setopt($ch, CURLOPT_TIMEOUT, 10);
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $curlError = curl_error($ch);
    curl_close($ch);
    
    // Debug: Show what was sent and received
    $debugInfo .= "<br>Sent to API: " . json_encode($data);
    $debugInfo .= "<br>HTTP Code: $httpCode";
    $debugInfo .= "<br>Response: $response";
    
    if ($response !== false) {
        $result = json_decode($response, true);
        
        if ($httpCode == 200 && isset($result['success']) && $result['success']) {
            // Authentication successful
            $_SESSION['user'] = $result['username'];
            header("Location: translate.php");
            exit();
        } else {
            // Authentication failed
            $error = isset($result['message']) ? $result['message'] : "Invalid credentials!";
        }
    } else {
        $error = "Unable to connect to authentication server. cURL Error: $curlError";
    }
}
?>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Darija</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background: url('moroccan-pattern.png') no-repeat center center fixed;
            background-size: cover;
            font-family: 'Lato', sans-serif;
        }
        .card {
            border-radius: 15px;
            overflow: hidden;
        }
        .card-header {
            background: linear-gradient(45deg, #1A3C6E, #CFA15D);
            color: #fff;
        }
        .btn-morocco {
            background: linear-gradient(45deg, #8B1E3F, #CFA15D);
            border: none;
            color: #fff;
            font-weight: bold;
            transition: transform 0.2s;
        }
        .btn-morocco:hover {
            transform: scale(1.05);
            background: linear-gradient(45deg, #CFA15D, #8B1E3F);
        }
        footer {
            margin-top: 20px;
            color: #fff;
            text-shadow: 1px 1px 2px #000;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="row justify-content-center mt-5">
            <div class="col-md-6">
                <div class="card shadow-lg">
                    <div class="card-header text-center">
                        <h2>Darija Translation</h2>
                        <p class="mb-0">Bienvenue / مرحبا بك</p>
                    </div>
                    <div class="card-body bg-light">
                        <form method="post" action="">
                            <div class="mb-3">
                                <label for="username" class="form-label">👤 Nom d'utilisateur</label>
                                <input 
                                    type="text" 
                                    id="username" 
                                    name="username" 
                                    class="form-control" 
                                    value="<?php echo isset($username) ? htmlspecialchars($username) : ''; ?>" 
                                    required>
                            </div>
                            <div class="mb-3">
                                <label for="password" class="form-label">🔑 Mot de passe</label>
                                <input 
                                    type="password" 
                                    id="password" 
                                    name="password" 
                                    class="form-control" 
                                    required>
                            </div>
                            <div class="d-grid">
                                <button type="submit" class="btn btn-morocco">Se connecter</button>
                            </div>
                        </form>
                        <div class="mt-3 text-center">
                            <small>Pas encore de compte ? <a href="signup.php">Inscrivez-vous ici</a></small>
                        </div>
                        <?php if (!empty($error)): ?>
                            <div class="alert alert-danger mt-3">
                                <?php echo htmlspecialchars($error); ?>
                            </div>
                        <?php endif; ?>
                        <?php if (isset($_SESSION['message'])): ?>
                            <div class="alert alert-success mt-3">
                                <?php echo htmlspecialchars($_SESSION['message']); ?>
                            </div>
                            <?php unset($_SESSION['message']); ?>
                        <?php endif; ?>
                        <!-- Debug info (à retirer en production) -->
                        <?php if (!empty($debugInfo) && $_SERVER["REQUEST_METHOD"] === "POST"): ?>
                            <div class="alert alert-info mt-3" style="font-size: 12px;">
                                <strong>Debug Info:</strong><br>
                                <?php echo $debugInfo; ?>
                            </div>
                        <?php endif; ?>
                    </div>
                </div>
                <footer class="text-center">
                    <small>🇲🇦 Made in Morocco | English → Darija Translator</small>
                </footer>
            </div>
        </div>
    </div>
</body>
</html>
