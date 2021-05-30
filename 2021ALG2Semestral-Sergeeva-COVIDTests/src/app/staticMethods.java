package app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import utils.DateHaveAlreadyExistException;
import utils.NameHaveAlreadyExistException;
import utils.NameHaveNotExistException;

/**
 *
 * @author Viktoriia Sergeeva
 */
public class staticMethods {

    public static Scanner sc = new Scanner(System.in);
    public static boolean konecProgramu = false;
    private static final String path = "data" + File.separator;
    static Places places = new Places();
    static Registrations reg = new Registrations();

    /**
     * Vapíše menu
     */
    public static void printMenu() {
        System.out.println("            Co chcete udělat?");
        System.out.println("1. Vytvořit rezervace termínu testování");
        System.out.println("2. Skupinová rezervace termínů");
        System.out.println("3. Upravit existující termín");
        System.out.println("4. Vymazat termín testování");
        System.out.println("5. Prohlednout seznam testovácích míst");
        System.out.println("6. Prohlednout seznam všech rezervací");
        System.out.println("7. Prohlednout seznam rezervací pro zvolené místo");
        System.out.println("8. Najít testovácí místo podle města a podminky");
        System.out.println("0. Konec programu");
        System.out.print("Zadejte zvolenou polozku menu: ");
    }

    public static void getInformationAboutPlaces() throws IOException {
        places.load(path + "fuuuul_dataOfPlaces.csv", ";", true);
        System.out.println("      Vítejte v rezervačním systému");
    }

    /**
     * Načítá volbu od uživatele
     *
     * @return
     */
    public static int loadingOption() {
        int option = -1;
        try {
            option = sc.nextInt();
        } catch (InputMismatchException e) {
            option = -1;
        } finally {
            sc.nextLine();
        }
        return option;
    }

    /**
     * Obsluhuje volbu uživatele
     *
     * @param choice
     * @throws IOException
     */
    public static void optionHandling(int choice) throws IOException {
        switch (choice) {
            case 0:
                konecProgramu = true;
                break;
            case 1:
                reserve();
                break;
            case 2:
                reserveForGroupe();
                break;
            case 3:
                updateTerm();
                break;
            case 4:
                deleteTerm();
                break;
            case 5:
                showAllPlaces();
                break;
            case 6:
                showAllReservations();
                break;
            case 7:
                showReservations();
                break;
            case 8:
                findPlaces();
                break;
            default:
                System.out.println("Chybne zadana volba");
        }
    }

    /**
     * Rezervace pro zadané jméno
     *
     * @throws IOException
     */
    public static void reserve() throws IOException {
        {
            System.out.println("Zadejte jméno a příjmení:");
            String nameOfPerson = sc.next() + " " + sc.next();
            System.out.println("Zadejte datum dd.mm.yyyy:");
            String date = sc.next();
            System.out.println("Znáte ID testovácího místa?");
            if (yesNoAnswer(sc.next()).equalsIgnoreCase("ano")) {
                System.out.println("Zadejte ID testovácího místa:");
                int ID = loadingOption();
                try {
                    reg.setRegistration(nameOfPerson, date, places.findPlaceByInternalID(ID));
                    System.out.println("Reservace proběhla úspěšně.");
                } catch (NameHaveAlreadyExistException e) {
                    System.out.println(e.getMessage());
                } catch (DateHaveAlreadyExistException e) {
                    System.out.println(e.getMessage());
                } catch (NoSuchElementException e) {
                    System.out.println(e.getMessage());
                } catch (DateTimeParseException e) {
                    System.out.println("Špatný format datumu");
                }
            } else {
                Places filteredPlaces = new Places();
                System.out.println("Napište název svého města.");
                String town = sc.next();
                System.out.format("%16s\n%s\n%s\n%s\n", "Zvolte podminku", "1. PCR", "2. Antigenic", "3. Drive in");
                switch (loadingOption()) {
                    case 1:
                        filteredPlaces = places.filterPlaces(town, "PCR");
                        break;
                    case 2:
                        filteredPlaces = places.filterPlaces(town, "Antigenic");
                        break;
                    case 3:
                        filteredPlaces = places.filterPlaces(town, "Drive_in");
                        break;
                }
                System.out.println(filteredPlaces.printPlaces());
                System.out.println("Napište ID zvoleného testovácího místa:");
                int ID = loadingOption();
                try {
                    reg.setRegistration(nameOfPerson, date, places.findPlaceByInternalID(ID));
                    System.out.println("Rezervace vytvořena úspěšně.");
                } catch (NoSuchElementException e) {
                    System.out.println("Testovácí místo s ID " + ID + " nenalezeno.");
                } catch (DateTimeParseException e) {
                    System.out.println("Špatný format datumu");
                }
            }
        }
    }

    /**
     * Skupinivá rezervace
     *
     * @throws IOException
     */
    public static void reserveForGroupe() throws IOException {
        System.out.println("Zadejte název souboru s daty ve formě nazev.type_souboru:");
        String name = sc.next();
        try {
            reg.load(path + name, ";", true, places);
            System.out.println("Náčtení dat proběhlo úspěšně.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (FileNotFoundException e) {
            System.out.println("Soubor s takovým názvem neexistuje.");
        }
    }

    /**
     * Aktualizuje termín na nový datum
     *
     * @throws IOException
     */
    public static void updateTerm() throws IOException {
        System.out.println("Zadejte jméno a příjmení:");
        String nameOfPerson = sc.next() + " " + sc.next();
        System.out.println("Zadejte nový datum dd.mm.yyyy:");
        String date = sc.next();
        System.out.println("Zadejte ID testovácího místa:");
        int internalID = loadingOption();

        try {
            reg.updateRegistrationDate(nameOfPerson, date, internalID);
            System.out.println("Rezervace termínu proběhla úspěšně.");
        } catch (NameHaveNotExistException e) {
            System.out.println(e.getMessage());
        } catch (DateHaveAlreadyExistException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Vymaže termín
     */
    public static void deleteTerm() {
        System.out.println("Zadejte jméno a příjmení:");
        String nameOfPerson = sc.next() + " " + sc.next();
        System.out.println("Zadejte ID testovácího místa:");
        int internalID = loadingOption();
        if (reg.deleteRegistrationForPerson(nameOfPerson, internalID)) {
            System.out.println("Termín byl uspěšně vymazan.");
        } else {
            System.out.println("Rezervace pro " + nameOfPerson + " neexistuje.");
        }
    }

    public static void showAllPlaces() throws IOException {
        System.out.println(places.printPlaces());
        System.out.println("Chcete uložit nelezený seznam do souboru?");
        if (yesNoAnswer(sc.next()).equalsIgnoreCase("ano")) {
            System.out.println("Zadejte název souboru:");
            places.save(sc.next());
        }
    }

    /**
     * Položka menu, která úkázuje uživazelovi seznam všech rezervací
     *
     * @throws IOException
     */
    public static void showAllReservations() throws IOException {
        System.out.println("Chcete setřídit seznam?");
        if (yesNoAnswer(sc.next()).equalsIgnoreCase("ano")) {
            System.out.println("Jak chcete setřídit seznam?\n1. Podle datumu\n2. Podle jména");
            switch (loadingOption()) {
                case 1:
                    System.out.println(reg.sortRegToStringByDate());
                    break;
                case 2:
                    System.out.println(reg.sortRegToStringByName());
                    break;
                default:
                    System.out.println("Chybne zadana volba");
            }
        } else {
            System.out.println(reg.toStringRegistrations());
        }
        System.out.println("Chcete uložit nelezený seznam do souboru?");
        if (yesNoAnswer(sc.next()).equalsIgnoreCase("ano")) {
            System.out.println("Zadejte název souboru bez typu:");
            String s = sc.next();
            System.out.println("Chcete uložit do PDF? ano/ne");
            String a = yesNoAnswer(sc.next());
            if (a.equalsIgnoreCase("ano")) {
                createPDF(s, reg);
            } else {
                reg.saveRegistrations(s, places);
            }
        }
    }

    /**
     * Položka menu, která úkázuje uživazelovi seznam vybráných rezervací
     *
     * @throws IOException
     */
    public static void showReservations() throws IOException {
        System.out.println("Zadejte ID testovácího místa:");
        int i = loadingOption();
        System.out.println("Chcete setřídit seznam?");
        if (yesNoAnswer(sc.next()).equalsIgnoreCase("ano")) {
            System.out.println("Jak chcete setřídit seznam?\n1. Podle datumu\n2. Podle jména");
            switch (loadingOption()) {
                case 1:
                    System.out.println(reg.sortRegToStringByDate(i));
                    break;
                case 2:
                    System.out.println(reg.sortRegToStringByName(i));
                    break;
                default:
                    System.out.println("Chybne zadana volba");
                    break;
            }
        } else {
            System.out.println(reg.toStringRegistrationsFor(i));
        }
        System.out.println("Chcete uložit nelezený seznam do souboru?");
        if (yesNoAnswer(sc.next()).equalsIgnoreCase("ano")) {
            System.out.println("Zadejte název souboru:");
            String s = sc.next();
            System.out.println("Chcete uložit do PDF?");
            if (yesNoAnswer(sc.next()).equalsIgnoreCase("ano")) {
                createPDF(s, reg);
            } else {
                reg.saveRegistrationsFor(i, s, places);
            }
        }
    }

    /**
     * Položka menu, která vyhledává místa
     *
     * @throws IOException
     */
    public static void findPlaces() throws IOException {
        Places filteredPlaces = new Places();
        System.out.println("Napište název svého města:");
        String town = sc.next();
        System.out.format("%16s\n%s\n%s\n%s\n", "Zvolte podminku", "1. PCR", "2. Antigenic", "3. Drive in");
        switch (loadingOption()) {
            case 1:
                filteredPlaces = places.filterPlaces(town, "PCR");
                break;
            case 2:
                filteredPlaces = places.filterPlaces(town, "Antigenic");
                break;
            case 3:
                filteredPlaces = places.filterPlaces(town, "Drive_in");
                break;
        }
        System.out.println(filteredPlaces.printPlaces());
        System.out.println("Chcete uložit nelezený seznam do souboru?");
        if (yesNoAnswer(sc.next()).equalsIgnoreCase("ano")) {
            System.out.println("Zadejte název souboru bez typu:");
            filteredPlaces.save(sc.next());
        }
    }

    /**
     * Vytvoří PDF soubor se seznamem rezervací
     *
     * @param nameOfFile
     * @param reg
     */
    public static void createPDF(String nameOfFile, Registrations reg) {
        String[] splited = reg.toStringRegistrations().split("\n");
        String filename = nameOfFile + ".pdf";
        int ir = splited.length;
        try {
            PDDocument doc = new PDDocument();
            PDDocumentInformation pdd = doc.getDocumentInformation();
            //autor
            pdd.setAuthor("COVID application");
            //title
            pdd.setTitle("Places");
            //creator
            pdd.setCreator("PDF Examples");
            //subject 
            pdd.setSubject("Places of testing");
            for (int j = 0; j < ir; j++) {
                int jj = j + 49;
                if (jj >= ir) {
                    jj = ir;
                }
                if (j % 49 == 0) {
                    int m = 725;
                    PDPage page = new PDPage();
                    doc.addPage(page);
                    PDPageContentStream content = new PDPageContentStream(doc, page);
                    content.beginText();
                    content.setFont(PDType1Font.HELVETICA, 12);
                    content.moveTextPositionByAmount(250, 750);
                    content.drawString("Seznam registrací");
                    content.endText();
                    for (int i = j; i < jj; i++) {
                        content.beginText();
                        content.moveTextPositionByAmount(70, m);
                        content.drawString(splited[i]);
                        m -= 15;
                        content.endText();
                    }
                    content.close();
                }
            }
            doc.save(filename);
            doc.close();
            System.out.println("PDF vytvořen v: " + System.getProperty("user.dir"));
        } catch (IOException | COSVisitorException e) {
            System.out.println(e.getMessage());
        }
    }

    public static String yesNoAnswer(String a) {
        while (!a.equalsIgnoreCase("ano") && !a.equalsIgnoreCase("ne")) {
            System.out.println("Odpovězte ano/ne:");
            a = sc.next();
        }
        return a;
    }
}
