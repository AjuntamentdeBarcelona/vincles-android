package cat.bcn.vincles.mobile.Client.Business;

public class UserBusiness {
    public boolean isUserAuthenticatedUserVincles (int idLibrary, int idCercle) {
        return !(idLibrary == -1 && idCercle == -1);
    }
}
