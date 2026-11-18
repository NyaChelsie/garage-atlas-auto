package com.example.garage_api.entity;

// Outils JPA. Nouveaux : CascadeType et OneToMany pour la liste de lignes.
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

// Outils de validation.
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import com.example.garage_api.enums.TypeIntervention;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Cette classe devient la table "intervention".
@Entity
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELATION : plusieurs interventions pour un vehicule.
    // La cle etrangere "vehicule_id" se trouve dans la table intervention.
    // LAZY : le vehicule n'est charge que si on le demande.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    // Type de travaux, enregistre par son NOM.
    @NotNull(message = "Le type d'intervention est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeIntervention type;

    // Description du travail : de 5 a 300 caracteres.
    @NotBlank(message = "La description est obligatoire")
    @Size(min = 5, max = 300, message = "La description doit contenir entre 5 et 300 caracteres")
    @Column(nullable = false, length = 300)
    private String description;

    // Date de debut : obligatoire, aujourd'hui ou avant.
    @NotNull(message = "La date de debut est obligatoire")
    @PastOrPresent(message = "La date de debut ne peut pas etre dans le futur")
    @Column(nullable = false)
    private LocalDate dateDebut;

    // Date de fin : facultative (l'intervention peut etre en cours).
    // La regle "pas avant le debut" sera verifiee dans le service.
    private LocalDate dateFin;

    // Cout de la main-d'oeuvre : jamais negatif. Facultatif dans le sujet.
    @DecimalMin(value = "0.0", message = "La main-d'oeuvre ne peut pas etre negative")
    @Column(precision = 12, scale = 2)
    private BigDecimal mainOeuvre;

    // RELATION INVERSE : une intervention possede plusieurs lignes (la "facture").
    // mappedBy = "intervention" : la cle etrangere est DEJA dans LigneIntervention,
    //   dans son champ nomme "intervention". Sans mappedBy, JPA creerait une
    //   deuxieme cle etrangere inutile (piege cite dans le sujet).
    // cascade = ALL : enregistrer ou supprimer l'intervention fait de meme pour ses lignes.
    // orphanRemoval = true : une ligne retiree de cette liste est supprimee de la base.
    // = new ArrayList<>() : la liste existe toujours, jamais null.
    @OneToMany(mappedBy = "intervention", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneIntervention> lignes = new ArrayList<>();

    // TOTAL : calcule a la demande, jamais stocke dans la base.
    // Il n'y a pas de champ "total" : cette methode ne devient donc pas une colonne.
    // Formule du sujet : mainOeuvre + somme (quantite x prix de chaque ligne).
    public BigDecimal getTotal() {
        // Si la main-d'oeuvre n'est pas renseignee, on la compte comme 0.
        BigDecimal base = (mainOeuvre == null) ? BigDecimal.ZERO : mainOeuvre;

        // Pour chaque ligne : prix x quantite. Puis on additionne le tout en partant de 0.
        BigDecimal totalLignes = lignes.stream()
                .map(l -> l.getPrixUnitaire().multiply(BigDecimal.valueOf(l.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return base.add(totalLignes);
    }

    // Methodes d'aide : elles gardent les DEUX cotes de la relation coherents.
    // Quand on ajoute une ligne a la liste, on dit aussi a la ligne a quelle
    // intervention elle appartient.
    public void ajouterLigne(LigneIntervention ligne) {
        lignes.add(ligne);
        ligne.setIntervention(this);
    }

    // Retirer une ligne de la liste la supprimera de la base (orphanRemoval).
    public void retirerLigne(LigneIntervention ligne) {
        lignes.remove(ligne);
        ligne.setIntervention(null);
    }

    // Getters et setters : a generer avec IntelliJ (voir plus bas).


    public Long getId() {
        return id;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public TypeIntervention getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public BigDecimal getMainOeuvre() {
        return mainOeuvre;
    }

    public List<LigneIntervention> getLignes() {
        return lignes;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }

    public void setType(TypeIntervention type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public void setMainOeuvre(BigDecimal mainOeuvre) {
        this.mainOeuvre = mainOeuvre;
    }

    public void setLignes(List<LigneIntervention> lignes) {
        this.lignes = lignes;
    }
}