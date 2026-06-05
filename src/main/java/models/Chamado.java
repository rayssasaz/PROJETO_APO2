package models;

import java.time.LocalDateTime;

public class Chamado {
	private int idChamado;
	private String titulo;
	private String descricao;
	private StatusChamado status;
	private LocalDateTime dataAbertura;
	private LocalDateTime dataAtualização;
	private Usuario cliente;
	private CategoriaChamado categoria;
	

	public Chamado() {
		
	}
	public Chamado(int idChamado, String titulo, String descricao, StatusChamado status, LocalDateTime dataAbertura,
			LocalDateTime dataAtualização, Usuario cliente, CategoriaChamado categoria) {
		
		this.idChamado = idChamado;
		this.titulo = titulo;
		this.descricao = descricao;
		this.status = status;
		this.dataAbertura = dataAbertura;
		this.dataAtualização = dataAtualização;
		this.cliente = cliente;
		this.categoria = categoria;
	}
	public int getIdChamado() {
		return idChamado;
	}
	public void setIdChamado(int idChamado) {
		this.idChamado = idChamado;
	}
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	public StatusChamado getStatus() {
		return status;
	}
	public void setStatus(StatusChamado status) {
		this.status = status;
	}
	public LocalDateTime getDataAbertura() {
		return dataAbertura;
	}
	public void setDataAbertura(LocalDateTime dataAbertura) {
		this.dataAbertura = dataAbertura;
	}
	public LocalDateTime getDataAtualização() {
		return dataAtualização;
	}
	public void setDataAtualização(LocalDateTime dataAtualização) {
		this.dataAtualização = dataAtualização;
	}
	public Usuario getCliente() {
		return cliente;
	}
	public void setCliente(Usuario cliente) {
		this.cliente = cliente;
	}
	public CategoriaChamado getCategoria() {
		return categoria;
	}
	public void setCategoria(CategoriaChamado categoria) {
		this.categoria = categoria;
	}
	
}
