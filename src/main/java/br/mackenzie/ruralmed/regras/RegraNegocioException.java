package br.mackenzie.ruralmed.regras;

/** Erro lançado quando uma regra de negócio é violada. Vira uma resposta HTTP 400 com a mensagem. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
