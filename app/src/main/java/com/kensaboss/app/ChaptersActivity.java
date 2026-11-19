
package com.kensaboss.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kensaboss.app.data.repository.ChapterRepository;
import com.kensaboss.app.localization.FontSizeManager;
import com.kensaboss.app.localization.LocaleManager;
import com.kensaboss.app.localization.ThemeManager;
import com.kensaboss.app.models.Chapter;

import java.util.List;

public class ChaptersActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_PART_ID = "book_part_id";
    public static final String EXTRA_BOOK_TITLE = "book_title";
    public static final String EXTRA_PART_TITLE = "part_title";

    private long bookPartId;

    private String bookTitle;
    private String partTitle;

    private LinearLayout chaptersListContainer;
    private TextView tvChaptersEmpty;

    private ChapterRepository chapterRepository;

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
        setContentView(R.layout.activity_chapters);

        bookPartId = getIntent().getLongExtra(EXTRA_BOOK_PART_ID, -1);
        bookTitle = getIntent().getStringExtra(EXTRA_BOOK_TITLE);
        partTitle = getIntent().getStringExtra(EXTRA_PART_TITLE);

        if (bookPartId <= 0) {
            finish();
            return;
        }

        if (bookTitle == null) bookTitle = "";
        if (partTitle == null) partTitle = "";

        initializeViews();
        setupClickListeners();

        chapterRepository = new ChapterRepository(this);

        loadChapters();
    }

    private void initializeViews() {
        TextView tvBookTitle = findViewById(R.id.tvChaptersBookTitle);
        TextView tvPartTitle = findViewById(R.id.tvChaptersPartTitle);

        chaptersListContainer = findViewById(R.id.chaptersListContainer);
        tvChaptersEmpty = findViewById(R.id.tvChaptersEmpty);

        tvBookTitle.setText(bookTitle);
        tvPartTitle.setText(partTitle);
    }

    private void setupClickListeners() {
        ImageButton btnBack = findViewById(R.id.btnChaptersBack);
        btnBack.setOnClickListener(view -> finish());
    }

    private void loadChapters() {
        chaptersListContainer.removeAllViews();

        List<Chapter> chapters =
                chapterRepository.getChaptersByBookPartId(bookPartId);

        if (chapters == null || chapters.isEmpty()) {
            tvChaptersEmpty.setVisibility(View.VISIBLE);
            return;
        }

        tvChaptersEmpty.setVisibility(View.GONE);

        for (Chapter chapter : chapters) {
            addChapterCard(chapter);
        }
    }

    private void addChapterCard(Chapter chapter) {
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

        TextView tvChapterTitle = new TextView(this);
        tvChapterTitle.setText(chapter.getTitle());
        tvChapterTitle.setTextColor(getColor(R.color.kb_text_primary));
        tvChapterTitle.setTextSize(17);
        tvChapterTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvChapterNumber = new TextView(this);
        tvChapterNumber.setText(
                getChapterNumberText(chapter.getChapterNumber())
        );
        tvChapterNumber.setTextColor(getColor(R.color.kb_text_secondary));
        tvChapterNumber.setTextSize(14);

        card.addView(tvChapterTitle);
        card.addView(tvChapterNumber);

        card.setOnClickListener(view -> {
            Intent intent = new Intent(
                    ChaptersActivity.this,
                    SectionsActivity.class
            );

            intent.putExtra(
                    SectionsActivity.EXTRA_CHAPTER_ID,
                    chapter.getId()
            );

            intent.putExtra(
                    SectionsActivity.EXTRA_BOOK_TITLE,
                    bookTitle
            );

            intent.putExtra(
                    SectionsActivity.EXTRA_PART_TITLE,
                    partTitle
            );

            intent.putExtra(
                    SectionsActivity.EXTRA_CHAPTER_TITLE,
                    chapter.getTitle()
            );

            startActivity(intent);
        });

        chaptersListContainer.addView(card);
    }

    private String getChapterNumberText(int number) {
        String language = getCurrentLanguage();

        switch (language) {
            case "hy":
                return "Գլուխ " + number;

            case "en":
                return "Chapter " + number;

            default:
                return "Глава " + number;
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
        if (chapterRepository != null) {
            chapterRepository.close();
        }

        super.onDestroy();
    }
}
