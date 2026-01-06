package br.com.fivecom.litoralfm.models.news;

import com.google.gson.annotations.SerializedName;

public class Categoria {

    private int id;

    @SerializedName("name")
    private String nome;

    private String slug;

    private Integer count;

    public Categoria(int id, String nome, String slug, Integer count) {
        this.id = id;
        this.nome = nome;
        this.slug = slug;
        this.count = count;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getSlug() { return slug; }
    public Integer getCount() { return count; }

    public void setId(int id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setSlug(String slug) { this.slug = slug; }
    public void setCount(Integer count) { this.count = count; }
}

