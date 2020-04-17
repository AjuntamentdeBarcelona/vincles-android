package cat.bcn.vincles.mobile.UI.ContentDetail;

public interface ContentDetailAugmentedView {
    int getCurrentPage();
    void showError(Object error);
    void showErrorOpeningImage();
    void contentAdded();
}

