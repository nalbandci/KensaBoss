
package com.kensaboss.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kensaboss.app.data.repository.SectionRepository;
import com.kensaboss.app.localization.FontSizeManager;
import com.kensaboss.app.localization.LocaleManager;
import com.kensaboss.app.localization.ThemeManager;
import com.kensaboss.app.models.Section;

import java.util.List;

public class SectionsActivity extends AppCompatActivity {

    public static final String EXTRA_CHAPTER_ID = "chapter_id";
    public static final String EXTRA_BOOK_TITLE = "book_title";
    public static final String EXTRA_PART_TITLE = "part_title";
    public static final String EXTRA_CHAPTER_TITLE = "chapter_title";

    private long chapterId;

    private String bookTitle;
    private String partTitle;
    private String chapterTitle;

    private LinearLayout sectionsListContainer;
    private TextView tvSectionsEmpty;

    private SectionRepository sectionRepository;

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
        setContentView(R.layout.activity_sections);

        chapterId = getIntent().getLongExtra(EXTRA_CHAPTER_ID, -1);
        bookTitle = getIntent().getStringExtra(EXTRA_BOOK_TITLE);
        partTitle = getIntent().getStringExtra(EXTRA_PART_TITLE);
        chapterTitle = getIntent().getStringExtra(EXTRA_CHAPTER_TITLE);

        if (chapterId <= 0) {
            finish();
            return;
        }

        if (bookTitle == null) bookTitle = "";
        if (partTitle == null) partTitle = "";
        if (chapterTitle == null) chapterTitle = "";

        initializeViews();
        setupClickListeners();

        sectionRepository = new SectionRepository(this);

        loadSections();
    }

    private void initializeViews() {
        TextView tvBookTitle = findViewById(R.id.tvSectionsBookTitle);
        TextView tvPartTitle = findViewById(R.id.tvSectionsPartTitle);
        TextView tvChapterTitle = findViewById(R.id.tvSectionsChapterTitle);

        sectionsListContainer = findViewById(R.id.sectionsListContainer);
        tvSectionsEmpty = findViewById(R.id.tvSectionsEmpty);

        tvBookTitle.setText(bookTitle);
        tvPartTitle.setText(partTitle);
        tvChapterTitle.setText(chapterTitle);
    }

    private void setupClickListeners() {
        ImageButton btnBack = findViewById(R.id.btnSectionsBack);
        btnBack.setOnClickListener(view -> finish());
    }

    private void loadSections() {
        sectionsListContainer.removeAllViews();

        List<Section> sections =
                sectionRepository.getSectionsByChapterId(chapterId);

        if (sections == null || sections.isEmpty()) {
            tvSectionsEmpty.setVisibility(View.VISIBLE);
            return;
        }

        tvSectionsEmpty.setVisibility(View.GONE);

        for (Section section : sections) {
            addSectionCard(section);
        }
    }

    private void addSectionCard(Section section) {
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

        TextView tvSectionTitle = new TextView(this);
        tvSectionTitle.setText(section.getTitle());
        tvSectionTitle.setTextColor(getColor(R.color.kb_text_primary));
        tvSectionTitle.setTextSize(17);
        tvSectionTitle.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        TextView tvSectionNumber = new TextView(this);
        tvSectionNumber.setText(
                getSectionNumberText(section.getSortOrder())
        );
        tvSectionNumber.setTextColor(
                getColor(R.color.kb_text_secondary)
        );
        tvSectionNumber.setTextSize(14);

        card.addView(tvSectionTitle);
        card.addView(tvSectionNumber);

        card.setOnClickListener(view -> {
            Intent intent = new Intent(
                    SectionsActivity.this,
                    QuestionsActivity.class
            );

            intent.putExtra(
                    QuestionsActivity.EXTRA_SECTION_ID,
                    section.getId()
            );

            intent.putExtra(
                    QuestionsActivity.EXTRA_BOOK_TITLE,
                    bookTitle
            );

            intent.putExtra(
                    QuestionsActivity.EXTRA_PART_TITLE,
                    partTitle
            );

            intent.putExtra(
                    QuestionsActivity.EXTRA_CHAPTER_TITLE,
                    chapterTitle
            );

            intent.putExtra(
                    QuestionsActivity.EXTRA_SECTION_TITLE,
                    section.getTitle()
            );

            startActivity(intent);
        });

        sectionsListContainer.addView(card);
    }

    private String getSectionNumberText(int number) {
        switch (getCurrentLanguage()) {
            case "hy":
                return "Բաժին " + number;

            case "en":
                return "Section " + number;

            default:
                return "Раздел " + number;
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
        if (sectionRepository != null) {
            sectionRepository.close();
        }

        super.onDestroy();
    }
}
