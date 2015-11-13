<?php
    require_once 'database.php';

    $result = ['result_type' => 'list', 'success' => false, 'message' => 'Erro ao obter denuncias', 'errors' => [], 'markers' => $markers];

    $dados = $_GET;
    $sql = 'SELECT *, DATE_FORMAT(`created`, "%d/%m/%Y") AS `date` FROM `notices` ORDER BY `created`;';
    try {
        $query = $conn->prepare($sql);
        if ($query->execute()) {
            $markers = $query->fetchAll();
            $result = ['result_type' => 'list', 'success' => true, 'errors' => [], 'markers' => $markers];
        }
    } catch (PDOException $e) {
        die($e->getMessage());
    }
    echo json_encode($result);
    exit(0);