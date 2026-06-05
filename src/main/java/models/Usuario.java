package models;

public class Usuario {
	private int idusuario;
	private String nome;
	private String email;
	private String senha;
	
	private NivelAcesso acesso;

	
	public Usuario() {
		
	}
	

	public Usuario(int idusuario, String nome, String email, String senha, NivelAcesso acesso) {
		this.idusuario = idusuario;
		this.nome = nome;
		this.email = email;
		this.senha = senha;
		this.acesso = acesso;
	}



	public int getIdusuario() {
		return idusuario;
	}

	public void setIdusuario(int idusuario) {
		this.idusuario = idusuario;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public NivelAcesso getAcesso() {
		return acesso;
	}

	public void setAcesso(NivelAcesso acesso) {
		this.acesso = acesso;
	}
	
	
	
	
	
}
