<?php
    require_once 'database.php';

    $result = ['result_type' => 'add', 'success' => false, 'message' => 'Erro ao salvar denuncia', 'errors' => []];

    $dados = $_POST;
    if (empty($dados['username'])) {
        $result['errors'][] = 'Preencha o nome';
    }
    if (empty($dados['useremail'])) {
        $result['errors'][] = 'Preencha o e-mail';
    } elseif (!filter_var($dados['useremail'], FILTER_VALIDATE_EMAIL)) {
        $result['errors'][] = 'Preencha um e-mail válido';
    }
    if (empty($dados['description'])) {
        $result['errors'][] = 'Preencha a descrição';
    }

    if (empty($result['errors'])) {
        $sql = 'INSERT INTO `notices` (`username`, `useremail`, `description`, `image`, `lat`, `lng`)
                VALUES (:username, :useremail, :description, :image, :lat, :lng);';
        try {

            $query = $conn->prepare($sql);
            $query->bindValue(':username', $dados['username']);
            $query->bindValue(':useremail', $dados['useremail']);
            $query->bindValue(':description', $dados['description']);
            $query->bindValue(':image', null /*$dados['image']*/);
            $query->bindValue(':lat', $dados['lat']);
            $query->bindValue(':lng', $dados['lng']);

            if ($query->execute()) {
                $sql = 'SELECT *, DATE_FORMAT(`created`, "%d/%m/%Y") AS `date` FROM `notices` ORDER BY `id` DESC LIMIT 1;';
                $query = $conn->prepare($sql);
                if ($query->execute()) {
                    $marker = $query->fetch();
                    $result = ['success' => true, 'message' => 'Denuncia salva com sucesso', 'errors' => [], 'marker' => $marker];
                }
            }
        } catch (PDOException $e) {
            die($e->getMessage());
        }
    }
    echo json_encode($result);
    exit(0);