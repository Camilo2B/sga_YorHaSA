package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** La temporada se guarda solo por su identificador (referencia entre agregados), sin llave foránea a un objeto. */
@Entity
@Table(name = "tarifa")
public class TarifaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apartamento_id", nullable = false)
    private ApartamentoEntity apartamento;

    @Column(name = "temporada_id", nullable = false)
    private UUID temporadaId;

    @Column(name = "valor_por_noche", nullable = false, precision = 19, scale = 0)
    private BigDecimal valorPorNoche;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDate vigenteDesde;

    protected TarifaEntity() {
    }

    public TarifaEntity(UUID id, UUID temporadaId, BigDecimal valorPorNoche, LocalDate vigenteDesde) {
        this.id = id;
        this.temporadaId = temporadaId;
        this.valorPorNoche = valorPorNoche;
        this.vigenteDesde = vigenteDesde;
    }

    void asignarApartamento(ApartamentoEntity apartamento) {
        this.apartamento = apartamento;
    }

    public UUID getId() { return id; }
    public UUID getTemporadaId() { return temporadaId; }
    public BigDecimal getValorPorNoche() { return valorPorNoche; }
    public LocalDate getVigenteDesde() { return vigenteDesde; }
}
