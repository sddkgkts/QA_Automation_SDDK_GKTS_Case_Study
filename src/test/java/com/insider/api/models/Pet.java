package com.insider.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

/**
 * Pet
 *
 * Entity model for the PetStore API.
 * Used in all CRUD operations against the {@code /pet} endpoints.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pet {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("category")
    private Category category;

    @JsonProperty("photoUrls")
    private String[] photoUrls;

    @JsonProperty("tags")
    private Tag[] tags;

    /** Valid values: available | pending | sold */
    @JsonProperty("status")
    private String status;

    public Pet() {}

    public Pet(Long id, String name, String[] photoUrls, String status) {
        this.id        = id;
        this.name      = name;
        this.photoUrls = photoUrls;
        this.status    = status;
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    /**
     * Create a minimal Pet object with the given name.
     *
     * @param name pet name
     * @return ready-to-use Pet instance
     */
    public static Pet createPet(String name) {
        Pet pet = new Pet();
        pet.setId(System.currentTimeMillis());
        pet.setName(name);
        pet.setPhotoUrls(new String[]{"https://example.com/pet.jpg"});
        pet.setStatus("available");
        return pet;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId()                { return id; }
    public void setId(Long id)         { this.id = id; }

    public String getName()            { return name; }
    public void setName(String name)   { this.name = name; }

    public Category getCategory()               { return category; }
    public void setCategory(Category category)  { this.category = category; }

    public String[] getPhotoUrls()                 { return photoUrls; }
    public void setPhotoUrls(String[] photoUrls)   { this.photoUrls = photoUrls; }

    public Tag[] getTags()             { return tags; }
    public void setTags(Tag[] tags)    { this.tags = tags; }

    public String getStatus()           { return status; }
    public void setStatus(String status){ this.status = status; }

    @Override
    public String toString() {
        return "Pet{id=" + id + ", name='" + name + "', status='" + status
            + "', photoUrls=" + Arrays.toString(photoUrls) + '}';
    }

    // ── Inner classes ─────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Category {
        @JsonProperty("id")   private Long id;
        @JsonProperty("name") private String name;

        public Long getId()           { return id; }
        public void setId(Long id)    { this.id = id; }
        public String getName()       { return name; }
        public void setName(String n) { this.name = n; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tag {
        @JsonProperty("id")   private Long id;
        @JsonProperty("name") private String name;

        public Long getId()           { return id; }
        public void setId(Long id)    { this.id = id; }
        public String getName()       { return name; }
        public void setName(String n) { this.name = n; }
    }
}
