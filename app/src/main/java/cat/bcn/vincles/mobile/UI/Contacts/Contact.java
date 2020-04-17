package cat.bcn.vincles.mobile.UI.Contacts;


import android.support.annotation.NonNull;

import java.util.Objects;

public class Contact implements Comparable<Contact> {

    public final static int TYPE_CIRCLE_USER = 0;
    public final static int TYPE_USER_CIRCLE = 1;
    public final static int TYPE_GROUP = 2;
    public final static int TYPE_DYNAMIZER = 3;

    private int type = 0;
    private int id;
    private int idChat = -1;
    private String name;
    private String lastname;
    private String state;
    private int idContentPhoto;
    private String path;
    private int numberNotifications = 0;
    private long numberInteractions = 0;
    private long lastInteraction;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public int getType() {
        return type;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public int getIdContentPhoto() {
        return idContentPhoto;
    }

    public void setIdContentPhoto(int idContentPhoto) {
        this.idContentPhoto = idContentPhoto;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getIdChat() {
        return idChat == -1 ? id : idChat;
    }

    public void setIdChat(int idChat) {
        this.idChat = idChat;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getNumberNotifications() {
        return numberNotifications;
    }

    public void setNumberNotifications(int numberNotifications) {
        this.numberNotifications = numberNotifications;
    }

    public long getNumberInteractions() {
        return numberInteractions;
    }

    public void setNumberInteractions(long numberInteractions) {
        this.numberInteractions = numberInteractions;
    }

    public long getLastInteraction() {
        return lastInteraction;
    }

    public void setLastInteraction(long lastInteraction) {
        this.lastInteraction = lastInteraction;
    }

    public int compare(Contact contact1, Contact contact2) {
        if (contact1.numberNotifications != contact2.numberNotifications) {
            return -(contact1.numberNotifications > contact2.numberNotifications ? 1 : -1);
        }
        if (contact1.lastInteraction != contact2.lastInteraction) {
            return -(contact1.lastInteraction > contact2.lastInteraction ? 1 : -1);
        }
        if (contact1.numberInteractions != contact2.numberInteractions) {
            return -(contact1.numberInteractions > contact2.numberInteractions ? 1 : -1);
        }
        if (!contact1.name.equals(contact2.name)) {
            return contact1.name.compareTo(contact2.name);
        }
        if (!contact1.lastname.equals(contact2.lastname)) {
            return contact1.lastname.compareTo(contact2.lastname);
        }
        return 0;
    }

    @Override
    public int compareTo(@NonNull Contact o) {
        return compare(this, o);
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Contact)) {
            return false;
        }
        Contact contact = (Contact) o;
        return id == contact.id &&
                Objects.equals(name, contact.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
