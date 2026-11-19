
package com.kensaboss.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.kensaboss.app.data.repository.BookRepository;
import com.kensaboss.app.localization.FontSizeManager;
import com.kensaboss.app.localization.LocaleManager;
import com.kensaboss.app.localization.ThemeManager;
import com.kensaboss.app.models.BookCatalogItem;

import java.util.List;
import java.util.Locale;

public class BooksActivity extends AppCompatActivity {

    private static final int MENU_DELETE_BOOK = 1;

    private ImageButton btnBack;

    private TextView tvBooksTitle;
    private TextView tvBooksDescription;
    private TextView tvBooksEmpty;

    private LinearLayout booksListContainer;

    private BookRepository bookRepository;

    @Override
    protected void attachBaseContext(Context newBase) {
        Context localizedContext = LocaleManager.wrap(newBase);
        Context fontContext = FontSizeManager.wrap(localizedContext);
        super.attachBaseContext(fontContext);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_books);

        initializeViews();
        setupListeners();

        bookRepository = new BookRepository(this);

        loadBooks();
    }

    @Override protected void onResume() { super.onResume(); if (bookRepository != null) loadBooks(); }
    private void initializeViews() {
        btnBack = findViewById(R.id.btnBooksBack);

        tvBooksTitle = findViewById(R.id.tvBooksTitle);
        tvBooksDescription = findViewById(R.id.tvBooksDescription);
        tvBooksEmpty = findViewById(R.id.tvBooksEmpty);

        booksListContainer = findViewById(R.id.booksListContainer);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(view -> finish());
    }

    private void loadBooks() {

        List<BookCatalogItem> books =
                bookRepository.getAllCatalogBooks();

        booksListContainer.removeAllViews();

        // Кнопка импорта учебного пакета.
        addImportButton();

        if (books.isEmpty()) {
            tvBooksEmpty.setVisibility(View.VISIBLE);
            booksListContainer.addView(tvBooksEmpty);
            return;
        }

        tvBooksEmpty.setVisibility(View.GONE);

        for (BookCatalogItem book : books) {
            addBookCard(book);
        }
    }

    /**
     * Добавляет кнопку импорта учебного пакета.
     * 
     */
    private void addImportButton() {
        Button button = new Button(this);
        String language = getResources().getConfiguration().locale.getLanguage();
        button.setText("en".equals(language) ? "Import study pack" : "hy".equals(language) ? "Ներմուծել փաթեթ" : "Добавить учебный пакет");
        booksListContainer.addView(button);
        button.setOnClickListener(v -> { Intent intent = new Intent(this, LearningHubActivity.class); intent.putExtra("mode", "import"); startActivity(intent); });
    }
    private void addBookCard(BookCatalogItem book) {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_surface);
        card.setPadding(
                dpToPx(18),
                dpToPx(18),
                dpToPx(18),
                dpToPx(18)
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = dpToPx(14);
        card.setLayoutParams(cardParams);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText(book.getTitle());
        title.setTextColor(getColor(R.color.kb_text_primary));
        title.setTextSize(19);
        title.setTypeface(null, android.graphics.Typeface.BOLD);

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        header.addView(title, titleParams);

        ImageButton btnMore = new ImageButton(this);
        btnMore.setImageResource(android.R.drawable.ic_menu_more);
        btnMore.setBackgroundResource(android.R.color.transparent);
        btnMore.setContentDescription(getMenuLabel());

        int buttonSize = dpToPx(40);

        LinearLayout.LayoutParams moreParams =
                new LinearLayout.LayoutParams(
                        buttonSize,
                        buttonSize
                );

        moreParams.leftMargin = dpToPx(8);

        header.addView(btnMore, moreParams);

        btnMore.setOnClickListener(view ->
                showBookMenu(btnMore, book)
        );

        card.addView(header);

        String authorText = book.getAuthor();

        if (authorText == null || authorText.trim().isEmpty()) {
            authorText = getString(R.string.book_author_unknown);
        }

        TextView author = new TextView(this);
        author.setText(authorText);
        author.setTextColor(getColor(R.color.kb_text_secondary));
        author.setTextSize(14);

        LinearLayout.LayoutParams authorParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        authorParams.topMargin = dpToPx(6);
        card.addView(author, authorParams);

        TextView parts = createInfoText(
                getString(
                        R.string.book_parts_count,
                        book.getPartsCount()
                )
        );

        TextView chapters = createInfoText(
                getString(
                        R.string.book_chapters_count,
                        book.getChaptersCount()
                )
        );

        TextView questions = createInfoText(
                getString(
                        R.string.book_questions_count,
                        book.getQuestionsCount()
                )
        );

        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        infoParams.topMargin = dpToPx(10);

        card.addView(parts, infoParams);
        card.addView(chapters);
        card.addView(questions);

        // Нажатие на карточку открывает книгу.
        card.setOnClickListener(view -> openBook(book));

        // Меню ⋮ открывает действия с книгой.
        btnMore.setOnClickListener(view ->
                showBookMenu(btnMore, book)
        );

        booksListContainer.addView(card);
    }

    private void openBook(BookCatalogItem book) {

        Intent intent = new Intent(
                BooksActivity.this,
                BookDetailsActivity.class
        );

        intent.putExtra(
                BookDetailsActivity.EXTRA_BOOK_ID,
                book.getId()
        );

        intent.putExtra(
                BookDetailsActivity.EXTRA_BOOK_TITLE,
                book.getTitle()
        );

        startActivity(intent);
    }

    private void showBookMenu(View anchor, BookCatalogItem book) {

        PopupMenu popupMenu = new PopupMenu(this, anchor);

        popupMenu.getMenu().add(
                Menu.NONE,
                MENU_DELETE_BOOK,
                Menu.NONE,
                getDeleteLabel()
        );

        popupMenu.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == MENU_DELETE_BOOK) {
                showDeleteConfirmation(book);
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

    private void showDeleteConfirmation(BookCatalogItem book) {

        new AlertDialog.Builder(this)
                .setTitle(getDeleteTitle())
                .setMessage(
                        getDeleteConfirmationMessage(book.getTitle())
                )
                .setNegativeButton(
                        getCancelLabel(),
                        (dialog, which) -> dialog.dismiss()
                )
                .setPositiveButton(
                        getDeleteLabel(),
                        (dialog, which) -> deleteBook(book)
                )
                .show();
    }

    private void deleteBook(BookCatalogItem book) {

        boolean deleted;

        try {
            deleted = bookRepository.deleteBook(book.getId());

        } catch (Exception exception) {

            Toast.makeText(
                    this,
                    getDeleteErrorMessage(),
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (deleted) {

            loadBooks();

            Toast.makeText(
                    this,
                    getDeleteSuccessMessage(),
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    this,
                    getDeleteErrorMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private TextView createInfoText(String text) {

        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(getColor(R.color.kb_text_secondary));
        view.setTextSize(13);

        return view;
    }

    private String getCurrentLanguage() {

        Locale locale = getResources()
                .getConfiguration()
                .locale;

        return locale == null ? "ru" : locale.getLanguage();
    }

    private String getMenuLabel() {
        switch (getCurrentLanguage()) {
            case "hy":
                return "Գործողություններ";
            case "en":
                return "Actions";
            default:
                return "Действия";
        }
    }

    private String getDeleteLabel() {
        switch (getCurrentLanguage()) {
            case "hy":
                return "Ջնջել";
            case "en":
                return "Delete";
            default:
                return "Удалить";
        }
    }

    private String getDeleteTitle() {
        switch (getCurrentLanguage()) {
            case "hy":
                return "Ջնջե՞լ գիրքը";
            case "en":
                return "Delete book?";
            default:
                return "Удалить книгу?";
        }
    }

    private String getDeleteConfirmationMessage(String bookTitle) {
        switch (getCurrentLanguage()) {
            case "hy":
                return "Դուք պատրաստվում եք ջնջել «"
                        + bookTitle
                        + "» գիրքը։ Դրան պատկանող ուսումնական նյութերը և պատասխանները կհեռացվեն։ Շարունակե՞լ?";

            case "en":
                return "You are about to delete \""
                        + bookTitle
                        + "\". Its learning materials and answers will be deleted. Continue?";

            default:
                return "Вы собираетесь удалить книгу «"
                        + bookTitle
                        + "». Её учебные материалы и ответы будут удалены. Продолжить?";
        }
    }

    private String getCancelLabel() {
        switch (getCurrentLanguage()) {
            case "hy":
                return "Չեղարկել";
            case "en":
                return "Cancel";
            default:
                return "Отмена";
        }
    }

    private String getDeleteSuccessMessage() {
        switch (getCurrentLanguage()) {
            case "hy":
                return "Գիրքը ջնջված է";
            case "en":
                return "Book deleted";
            default:
                return "Книга удалена";
        }
    }

    private String getDeleteErrorMessage() {
        switch (getCurrentLanguage()) {
            case "hy":
                return "Չհաջողվեց ջնջել գիրքը";
            case "en":
                return "Could not delete the book";
            default:
                return "Не удалось удалить книгу";
        }
    }

    private int dpToPx(int dp) {
        return (int) (
                dp * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bookRepository != null) {
            bookRepository.close();
        }
    }
}

