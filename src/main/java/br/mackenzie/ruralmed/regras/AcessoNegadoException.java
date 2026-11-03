package br.mackenzie.ruralmed.regras;

/** Usuário sem login ou sem permissão para a operação. Vira uma resposta HTTP 403. */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
