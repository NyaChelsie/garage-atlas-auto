package com.example.garage_api.config;

import com.example.garage_api.entity.Client;
import com.example.garage_api.entity.Intervention;
import com.example.garage_api.entity.LigneIntervention;
import com.example.garage_api.entity.Piece;
import com.example.garage_api.entity.Vehicule;
import com.example.garage_api.enums.Carburant;
import com.example.garage_api.enums.CategoriePiece;
import com.example.garage_api.enums.StatutVehicule;
import com.example.garage_api.enums.TypeIntervention;
import com.example.garage_api.repository.ClientRepository;
import com.example.garage_api.repository.InterventionRepository;
import com.example.garage_api.repository.LigneInterventionRepository;
import com.example.garage_api.repository.PieceRepository;
import com.example.garage_api.repository.VehiculeRepository;

// CommandLineRunner : une interface Spring. Tout objet qui l'implemente voit sa
// methode run() executee UNE FOIS, juste apres le demarrage du back.
import org.springframework.boot.CommandLineRunner;

// Component : "Spring, cree un objet de cette classe et gere-le toi-meme".
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Cette classe s'execute avec TOUS les profils (dev et prod), mais elle ne charge
// les donnees que si la base est VIDE (voir le test au debut de run).
// Ainsi, avec MySQL, les donnees survivent aux redemarrages : rien n'est recree.
@Component
public class DonneesDepart implements CommandLineRunner {

    // Les "magasiniers" dont on a besoin. final = on ne les remplace jamais.
    private final ClientRepository clientRepository;
    private final VehiculeRepository vehiculeRepository;
    private final PieceRepository pieceRepository;
    private final InterventionRepository interventionRepository;
    private final LigneInterventionRepository ligneRepository;

    // Constructeur : Spring nous donne lui-meme les 5 repositories au moment de creer l'objet.
    // C'est l'injection de dependances : on ne fait jamais "new ClientRepository()".
    public DonneesDepart(ClientRepository clientRepository,
                         VehiculeRepository vehiculeRepository,
                         PieceRepository pieceRepository,
                         InterventionRepository interventionRepository,
                         LigneInterventionRepository ligneRepository) {
        this.clientRepository = clientRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.pieceRepository = pieceRepository;
        this.interventionRepository = interventionRepository;
        this.ligneRepository = ligneRepository;
    }

    // run : executee automatiquement au demarrage.
    // L'ORDRE compte : on cree d'abord ce dont les autres ont besoin.
    //   client -> vehicule -> piece -> intervention -> lignes
    @Override
    public void run(String... args) {
        // Si la table des clients contient deja des lignes, la base est deja remplie :
        // on ne recharge rien (sinon on aurait des doublons, ou une erreur d'unicite).
        if (clientRepository.count() > 0) {
            System.out.println("Donnees de depart : base deja remplie, rien a charger.");
            return; // on quitte la methode ici
        }

        List<Client> clients = creerClients();
        List<Vehicule> vehicules = creerVehicules(clients);
        // Map : un dictionnaire "reference -> piece", pour retrouver une piece
        // par sa reference quand on cree les lignes.
        Map<String, Piece> pieces = creerPieces();
        creerInterventions(vehicules, pieces);

        // Message de controle dans la console IntelliJ.
        // count() = le nombre de lignes dans la table.
        System.out.println("Donnees de depart : "
                + clientRepository.count() + " clients, "
                + vehiculeRepository.count() + " vehicules, "
                + pieceRepository.count() + " pieces, "
                + interventionRepository.count() + " interventions, "
                + ligneRepository.count() + " lignes");
    }

    // Cree les 5 clients et les renvoie dans une liste (dans l'ordre de creation).
    private List<Client> creerClients() {
        return List.of(
                creerClient("Mbarga", "Jean-Paul", "677112233", "jp.mbarga@example.com", "Douala", 400),
                creerClient("Ngo", "Marie", "698445566", "marie.ngo@example.com", "Yaoundé", 300),
                creerClient("Tchoumi", "Patrick", "655778899", null, "Douala", 200),
                creerClient("Fotso", "Alice", "699001122", "alice.fotso@example.com", "Bafoussam", 120),
                // Ce dernier client n'aura AUCUN vehicule (exige par le sujet).
                creerClient("Kamga", "Didier", "670334455", null, "Douala", 45)
        );
    }

    // Methode d'aide : fabrique UN client et l'enregistre.
    private Client creerClient(String nom, String prenom, String telephone,
                               String email, String ville, int joursDepuisInscription) {
        Client client = new Client();
        client.setNom(nom);
        client.setPrenom(prenom);
        client.setTelephone(telephone);
        client.setEmail(email);
        client.setVille(ville);
        // LocalDate.now() = aujourd'hui. minusDays(n) = n jours avant.
        // Ainsi la date n'est jamais dans le futur, quel que soit le jour de lancement.
        client.setDateInscription(LocalDate.now().minusDays(joursDepuisInscription));
        // save : enregistre dans la base et renvoie le client AVEC son id genere.
        return clientRepository.save(client);
    }

    // Cree les 8 vehicules et les renvoie dans une liste, dans cet ordre :
    // 0 Corolla, 1 Hilux, 2 Clio, 3 308, 4 Leaf, 5 Tucson, 6 Sportage, 7 Civic.
    private List<Vehicule> creerVehicules(List<Client> clients) {
        Client mbarga = clients.get(0);
        Client ngo = clients.get(1);
        Client tchoumi = clients.get(2);
        Client fotso = clients.get(3);
        // clients.get(4) (Kamga) n'est volontairement jamais utilise : il n'a pas de vehicule.

        return List.of(
                // Mbarga possede 3 vehicules (exige par le sujet).
                creerVehicule(mbarga, "AB-123-CD", "Toyota", "Corolla", 2018, 85000,
                        Carburant.ESSENCE, StatutVehicule.EN_REPARATION, 10, "85000", "Bruit au freinage"),
                creerVehicule(mbarga, "CD-456-EF", "Toyota", "Hilux", 2020, 62000,
                        Carburant.DIESEL, StatutVehicule.EN_ATTENTE, 2, "120000", "Voyant moteur allumé"),
                creerVehicule(mbarga, "EF-789-GH", "Renault", "Clio", 2016, 140000,
                        Carburant.ESSENCE, StatutVehicule.LIVRE, 60, "35000", "Vidange complète"),

                creerVehicule(ngo, "GH-234-IJ", "Peugeot", "308", 2019, 98000,
                        Carburant.DIESEL, StatutVehicule.TERMINE, 25, "60000", "Remplacement des plaquettes"),
                creerVehicule(ngo, "IJ-567-KL", "Nissan", "Leaf", 2022, 30000,
                        Carburant.ELECTRIQUE, StatutVehicule.EN_ATTENTE, 1, "150000", "Autonomie réduite"),

                creerVehicule(tchoumi, "KL-890-MN", "Hyundai", "Tucson", 2021, 45000,
                        Carburant.HYBRIDE, StatutVehicule.EN_REPARATION, 8, "95000", "Suspension avant bruyante"),

                creerVehicule(fotso, "MN-111-OP", "Kia", "Sportage", 2017, 110000,
                        Carburant.DIESEL, StatutVehicule.TERMINE, 30, "70000", "Batterie à remplacer"),
                creerVehicule(fotso, "OP-222-QR", "Honda", "Civic", 2015, 160000,
                        Carburant.ESSENCE, StatutVehicule.LIVRE, 90, "50000", "Révision générale")
        );
    }

    // Methode d'aide : fabrique UN vehicule et l'enregistre.
    private Vehicule creerVehicule(Client proprietaire, String immatriculation,
                                   String marque, String modele, int annee, int kilometrage,
                                   Carburant carburant, StatutVehicule statut,
                                   int joursDepuisEntree, String coutEstime, String panne) {
        Vehicule vehicule = new Vehicule();
        vehicule.setClient(proprietaire);             // le lien : sa cle etrangere client_id
        vehicule.setImmatriculation(immatriculation);
        vehicule.setMarque(marque);
        vehicule.setModele(modele);
        vehicule.setAnnee(annee);
        vehicule.setKilometrage(kilometrage);
        vehicule.setCarburant(carburant);
        vehicule.setStatut(statut);
        vehicule.setDateEntree(LocalDate.now().minusDays(joursDepuisEntree));
        // new BigDecimal("85000") a partir d'un TEXTE : exact. Jamais a partir d'un double.
        vehicule.setCoutEstime(new BigDecimal(coutEstime));
        vehicule.setPanne(panne);
        return vehiculeRepository.save(vehicule);
    }

    // Cree les 10 pieces et les renvoie dans un dictionnaire reference -> piece.
    // LinkedHashMap : garde l'ordre d'insertion (pratique pour relire).
    private Map<String, Piece> creerPieces() {
        Map<String, Piece> pieces = new LinkedHashMap<>();

        // Arguments : reference, designation, categorie, fabricant, prix,
        //             stock de depart, seuil d'alerte, jours depuis l'achat.
        ajouter(pieces, "PCE-0001", "Plaquettes de frein avant", CategoriePiece.FREINAGE,
                "Bosch", "15000", 20, 5, 120);
        ajouter(pieces, "PCE-0002", "Disque de frein avant", CategoriePiece.FREINAGE,
                "Brembo", "32000", 5, 4, 100);
        ajouter(pieces, "PCE-0003", "Liquide de frein DOT4", CategoriePiece.FREINAGE,
                "Castrol", "6000", 12, 4, 90);
        ajouter(pieces, "PCE-0004", "Filtre à huile", CategoriePiece.ENTRETIEN,
                "Mann-Filter", "4500", 25, 8, 150);
        ajouter(pieces, "PCE-0005", "Huile moteur 5W30 (5 L)", CategoriePiece.ENTRETIEN,
                "Total", "18000", 15, 6, 80);
        ajouter(pieces, "PCE-0006", "Filtre à air", CategoriePiece.ENTRETIEN,
                "Mann-Filter", "7500", 18, 6, 70);
        ajouter(pieces, "PCE-0007", "Amortisseur avant", CategoriePiece.SUSPENSION,
                "Monroe", "45000", 4, 5, 60);
        ajouter(pieces, "PCE-0008", "Rotule de suspension", CategoriePiece.SUSPENSION,
                "Lemförder", "12000", 10, 4, 55);
        ajouter(pieces, "PCE-0009", "Batterie 12V 60Ah", CategoriePiece.ELECTRICITE,
                "Varta", "55000", 3, 3, 40);
        ajouter(pieces, "PCE-0010", "Bougie d'allumage", CategoriePiece.ELECTRICITE,
                "NGK", "3500", 40, 12, 30);

        return pieces;
    }

    // Methode d'aide : fabrique UNE piece, l'enregistre, et la range dans le dictionnaire.
    private void ajouter(Map<String, Piece> pieces, String reference, String designation,
                         CategoriePiece categorie, String fabricant, String prix,
                         int stock, int seuil, int joursDepuisAchat) {
        Piece piece = new Piece();
        piece.setReference(reference);
        piece.setDesignation(designation);
        piece.setCategorie(categorie);
        piece.setFabricant(fabricant);
        // Le prix vient d'un TEXTE : BigDecimal exact, sans erreur d'arrondi.
        piece.setPrixUnitaire(new BigDecimal(prix));
        piece.setQuantiteStock(stock);
        piece.setSeuilAlerte(seuil);
        piece.setDateAchat(LocalDate.now().minusDays(joursDepuisAchat));
        // save renvoie la piece AVEC son id ; on la range sous sa reference.
        pieces.put(reference, pieceRepository.save(piece));
    }

    // Cree les 6 interventions et leurs 10 lignes.
    private void creerInterventions(List<Vehicule> vehicules, Map<String, Piece> pieces) {
        // On nomme les vehicules pour que le code se lise comme une phrase.
        // (Hilux = get(1) et Leaf = get(4) n'ont volontairement aucune intervention.)
        Vehicule corolla = vehicules.get(0);
        Vehicule clio = vehicules.get(2);
        Vehicule peugeot = vehicules.get(3);
        Vehicule tucson = vehicules.get(5);
        Vehicule sportage = vehicules.get(6);
        Vehicule civic = vehicules.get(7);

        // Intervention 1 : freins de la Peugeot, avec 3 lignes.
        Intervention i1 = nouvelleIntervention(peugeot, TypeIntervention.FREINS,
                "Remplacement des plaquettes et des disques avant", 24, 20, "20000");
        ajouterLigneEtDeduireStock(i1, pieces.get("PCE-0001"), 2);
        ajouterLigneEtDeduireStock(i1, pieces.get("PCE-0002"), 2);
        ajouterLigneEtDeduireStock(i1, pieces.get("PCE-0003"), 1);
        // save sur l'intervention enregistre AUSSI ses lignes (cascade = ALL).
        interventionRepository.save(i1);

        // Intervention 2 : vidange de la Clio, avec 2 lignes.
        Intervention i2 = nouvelleIntervention(clio, TypeIntervention.VIDANGE,
                "Vidange complète du moteur", 59, 58, "8000");
        ajouterLigneEtDeduireStock(i2, pieces.get("PCE-0004"), 1);
        ajouterLigneEtDeduireStock(i2, pieces.get("PCE-0005"), 1);
        interventionRepository.save(i2);

        // Intervention 3 : suspension du Tucson, EN COURS (pas de date de fin : null).
        Intervention i3 = nouvelleIntervention(tucson, TypeIntervention.DIAGNOSTIC,
                "Diagnostic et remplacement de la suspension avant", 7, null, "30000");
        ajouterLigneEtDeduireStock(i3, pieces.get("PCE-0007"), 1);
        ajouterLigneEtDeduireStock(i3, pieces.get("PCE-0008"), 1);
        interventionRepository.save(i3);

        // Intervention 4 : batterie de la Sportage, avec 1 ligne.
        Intervention i4 = nouvelleIntervention(sportage, TypeIntervention.ELECTRICITE,
                "Remplacement de la batterie", 29, 28, "10000");
        ajouterLigneEtDeduireStock(i4, pieces.get("PCE-0009"), 1);
        interventionRepository.save(i4);

        // Intervention 5 : revision de la Civic, avec 2 lignes (4 bougies + 1 filtre).
        Intervention i5 = nouvelleIntervention(civic, TypeIntervention.MOTEUR,
                "Révision générale : bougies et filtre à huile", 89, 88, "25000");
        ajouterLigneEtDeduireStock(i5, pieces.get("PCE-0010"), 4);
        ajouterLigneEtDeduireStock(i5, pieces.get("PCE-0004"), 1);
        interventionRepository.save(i5);

        // Intervention 6 : diagnostic de la Corolla, EN COURS et SANS aucune ligne
        // (une intervention peut exister avant que des pieces soient ajoutees).
        Intervention i6 = nouvelleIntervention(corolla, TypeIntervention.DIAGNOSTIC,
                "Diagnostic du bruit au freinage", 9, null, "5000");
        interventionRepository.save(i6);
    }

    // Methode d'aide : fabrique UNE intervention (sans l'enregistrer).
    // joursFin est un Integer (pas un int) : il peut valoir null = "pas encore terminee".
    private Intervention nouvelleIntervention(Vehicule vehicule, TypeIntervention type,
                                              String description, int joursDepuisDebut,
                                              Integer joursDepuisFin, String mainOeuvre) {
        Intervention intervention = new Intervention();
        intervention.setVehicule(vehicule);
        intervention.setType(type);
        intervention.setDescription(description);
        intervention.setDateDebut(LocalDate.now().minusDays(joursDepuisDebut));
        // Operateur ternaire : "si joursDepuisFin est null alors null, sinon la date calculee".
        intervention.setDateFin(joursDepuisFin == null
                ? null
                : LocalDate.now().minusDays(joursDepuisFin));
        intervention.setMainOeuvre(new BigDecimal(mainOeuvre));
        return intervention;
    }

    // Methode d'aide : ajoute une ligne a l'intervention ET diminue le stock de la piece.
    // C'est exactement la regle que le service appliquera a l'etape 3.
    private void ajouterLigneEtDeduireStock(Intervention intervention, Piece piece, int quantite) {
        // Garde-fou : on ne sort jamais plus que ce qu'il y a en stock.
        if (piece.getQuantiteStock() < quantite) {
            throw new IllegalStateException("Stock insuffisant pour " + piece.getReference());
        }

        LigneIntervention ligne = new LigneIntervention();
        ligne.setPiece(piece);
        ligne.setQuantite(quantite);
        // Le prix est COPIE depuis la piece : la facture garde ce prix pour toujours.
        ligne.setPrixUnitaire(piece.getPrixUnitaire());
        // ajouterLigne relie les deux cotes : la liste de l'intervention ET le champ de la ligne.
        intervention.ajouterLigne(ligne);

        // Le stock baisse de la quantite utilisee, puis on enregistre la piece.
        piece.setQuantiteStock(piece.getQuantiteStock() - quantite);
        pieceRepository.save(piece);
    }
}