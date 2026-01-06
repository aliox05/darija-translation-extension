<?php
session_start();
if (!isset($_SESSION['user'])) {
    header("Location: login.php");
    exit();
}
if (isset($_POST['logout'])) {
    session_destroy();
    header("Location: login.php");
    exit();
}

$translationResult = "";
$detectedLang = "";
if (isset($_POST['inputText'])) {
    $inputText = $_POST['inputText'];

    $apiUrl = "http://localhost:8082/English-darija-tr/rest/translate";
    $data = array("text" => $inputText); // backend detects language
    $dataJson = json_encode($data);

    $ch = curl_init($apiUrl);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, array(
        'Content-Type: application/json',
        'Accept: application/json'
    ));
    curl_setopt($ch, CURLOPT_POSTFIELDS, $dataJson);
    $response = curl_exec($ch);

    if ($response === false) {
        $translationResult = "Error: Unable to connect to API.";
    } else {
        $result = json_decode($response, true);
        if ($result && isset($result['translation'])) {
            $translationResult = $result['translation'];
            $detectedLang = $result['sourceLang'] ?? "unknown";
        } else {
            $translationResult = "Error: Invalid response. API Response: " . htmlspecialchars($response);
        }
    }
    curl_close($ch);
}

// Language code → full name mapping
$langNames = [
    "en" => "English",
    "fr" => "French",
    "es" => "Spanish",
    "ar" => "Arabic",
    "de" => "German",
    "it" => "Italian",
    "pt" => "Portuguese",
    "ru" => "Russian",
    "zh" => "Chinese",
    "ja" => "Japanese",
    "tr" => "Turkish",
    "nl" => "Dutch",
    "pl" => "Polish",
    "sv" => "Swedish",
    "no" => "Norwegian",
    "fi" => "Finnish",
    "el" => "Greek",
    "hi" => "Hindi",
    "ko" => "Korean",
    "unknown" => "Unknown Language"
];

$detectedLangName = $langNames[$detectedLang] ?? $detectedLang;

// Split translation into Arabic + Latin lines
$arabicLine = "";
$latinLine = "";
if (!empty($translationResult)) {
    $lines = preg_split("/\r\n|\r|\n/", $translationResult);
    if (count($lines) >= 2) {
        $arabicLine = trim($lines[0]);
        $latinLine = trim($lines[1]);
    } else {
        // fallback: show whole translation if not split
        $arabicLine = $translationResult;
    }
}
?>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Darija Translator</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background: url('moroccan-pattern.png') no-repeat center center fixed;
            background-size: cover;
            font-family: 'Lato', sans-serif;
        }
        .card { border-radius: 15px; overflow: hidden; }
        .card-header {
            background: linear-gradient(45deg, #1A3C6E, #CFA15D);
            color: #fff;
        }
        .btn-morocco {
            background: linear-gradient(45deg, #8B1E3F, #CFA15D);
            border: none; color: #fff; font-weight: bold;
            transition: transform 0.2s;
        }
        .btn-morocco:hover {
            transform: scale(1.05);
            background: linear-gradient(45deg, #CFA15D, #8B1E3F);
        }
        footer { margin-top: 20px; color: #fff; text-shadow: 1px 1px 2px #000; }
    </style>
</head>
<body>
<div class="container">
    <div class="row justify-content-center mt-5">
        <div class="col-md-8">
            <div class="card shadow-lg">
                <div class="card-header text-center">
                    <h2>ⵣ Multilingual → Darija Translator</h2>
                    <p class="mb-0">Bienvenue, <?php echo htmlspecialchars($_SESSION['user']); ?> / مرحبا بك</p>
                </div>
                <div class="card-body bg-light">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <form method="post" class="d-inline">
                            <button type="submit" name="logout" class="btn btn-danger btn-sm">Logout</button>
                        </form>
                    </div>
                    <p class="mb-3">Type text in any language to translate into Darija:</p>
                    <form method="post">
                        <div class="input-group mb-3">
                            <input type="text" name="inputText" class="form-control" placeholder="Enter text" required>
                            <button type="submit" class="btn btn-morocco">Translate</button>
                        </div>
                    </form>
                    <?php if (!empty($translationResult)): ?>
                        <div class="alert alert-info">
                            <?php if (!empty($detectedLang)): ?>
                                <strong>Detected Language:</strong> <?php echo htmlspecialchars($detectedLangName); ?><br>
                            <?php endif; ?>
                            <?php if (!empty($arabicLine)): ?>
                                <strong>Darija (Arabic):</strong> <?php echo htmlspecialchars($arabicLine); ?><br>
                            <?php endif; ?>
                            <?php if (!empty($latinLine)): ?>
                                <strong>Darija (Latin):</strong> <?php echo htmlspecialchars($latinLine); ?>
                            <?php endif; ?>
                        </div>
                    <?php endif; ?>
                </div>
            </div>
            <footer class="text-center">
                <small>🇲🇦 Made in Morocco | Multilingual → Darija Translator</small>
            </footer>
        </div>
    </div>
</div>
</body>
</html>
