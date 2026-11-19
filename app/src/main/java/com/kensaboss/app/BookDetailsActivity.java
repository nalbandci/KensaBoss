
package com.kensaboss.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kensaboss.app.data.repository.BookPartRepository;
import com.kensaboss.app.localization.FontSizeManager;
import com.kensaboss.app.localization.LocaleManager;
import com.kensaboss.app.localization.ThemeManager;
import com.kensaboss.app.models.BookPart;

import java.util.List;

public class BookDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID = "book_id";
    public static final String EXTRA_BOOK_TITLE = "book_title";

    private long bookId;
    private String bookTitle;

    private LinearLayout partsListContainer;
    private TextView tvPartsEmpty;

    private BookPartRepository bookPartRepository;

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
        setContentView(R.layout.activity_book_details);

        bookId = getIntent().getLongExtra(EXTRA_BOOK_ID, -1);
        bookTitle = getIntent().getStringExtra(EXTRA_BOOK_TITLE);

        if (bookId <= 0) {
            finish();
            return;
        }

        if (bookTitle == null) {
            bookTitle = "";
        }

        initializeViews();
        setupClickListeners();

        bookPartRepository = new BookPartRepository(this);

        loadBookParts();
    }

    private void initializeViews() {
        ImageButton btnBack = findViewById(R.id.btnBookDetailsBack);
        TextView tvTitle = findViewById(R.id.tvBookDetailsTitle);

        partsListContainer = findViewById(R.id.partsListContainer);
        tvPartsEmpty = findViewById(R.id.tvPartsEmpty);

        tvTitle.setText(bookTitle);
    }

    private void setupClickListeners() {
        ImageButton btnBack = findViewById(R.id.btnBookDetailsBack);

        btnBack.setOnClickListener(view -> finish());
    }

    private void loadBookParts() {
        partsListContainer.removeAllViews();

        List<BookPart> parts =
                bookPartRepository.getBookPartsByBookId(bookId);

        if (parts == null || parts.isEmpty()) {
            tvPartsEmpty.setVisibility(View.VISIBLE);
            return;
        }

        tvPartsEmpty.setVisibility(View.GONE);

        for (BookPart part : parts) {
            addPartCard(part);
        }
    }

    private void addPartCard(BookPart part) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.setBackgroundResource(R.drawable.bg_card_surface);
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);

        TextView tvPartTitle = new TextView(this);
        tvPartTitle.setText(part.getTitle());
        tvPartTitle.setTextColor(
                getColor(R.color.kb_text_primary)
        );
        tvPartTitle.setTextSize(17);
        tvPartTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvPartNumber = new TextView(this);
        tvPartNumber.setText(
                getPartNumberText(part.getPartNumber())
        );
        tvPartNumber.setTextColor(
                getColor(R.color.kb_text_secondary)
        );
        tvPartNumber.setTextSize(14);

        card.addView(tvPartTitle);
        card.addView(tvPartNumber);

        card.setOnClickListener(view -> {
            Intent intent = new Intent(
                    BookDetailsActivity.this,
                    ChaptersActivity.class
            );

            intent.putExtra(
                    ChaptersActivity.EXTRA_BOOK_PART_ID,
                    part.getId()
            );

            intent.putExtra(
                    ChaptersActivity.EXTRA_BOOK_TITLE,
                    bookTitle
            );

            intent.putExtra(
                    ChaptersActivity.EXTRA_PART_TITLE,
                    part.getTitle()
            );

            startActivity(intent);
        });

        partsListContainer.addView(card);
    }

    private String getPartNumberText(int number) {
        String language = getCurrentLanguage();

        switch (language) {
            case "hy":
                return "Մաս " + number;

            case "en":
                return "Part " + number;

            default:
                return "Часть " + number;
        }
    }

    private String getCurrentLanguage() {
        return getResources()
                .getConfiguration()
                .locale
                .getLanguage();
    }

    private int dp(int value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    @Override
    protected void onDestroy() {
        if (bookPartRepository != null) {
            bookPartRepository.close();
        }

        super.onDestroy();
    }
}
