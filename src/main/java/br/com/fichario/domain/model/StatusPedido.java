package br.com.fichario.domain.model;

/** Etapas pelas quais um pedido passa, do registro ate a entrega. */
public enum StatusPedido {
    RECEBIDO,
    AGUARDANDO_MATERIAL,
    EM_PRODUCAO,
    PRONTO,
    ENTREGUE,
    CANCELADO;

    /** Pedido aberto ainda pode ser alterado, pago e produzido. */
    public boolean aberto() {
        return this != ENTREGUE && this != CANCELADO;
    }
}
