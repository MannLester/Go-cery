package com.example.mobcompfinalproj;

//general imports
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.graphics.Typeface;

public class ReceiptActivity extends AppCompatActivity {
    private TextView dateText;
    private TextView sellerIdText;
    private TextView totalText;
    private RecyclerView receiptRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        dateText = findViewById(R.id.dateText);
        sellerIdText = findViewById(R.id.sellerIdText);
        totalText = findViewById(R.id.totalText);
        receiptRecyclerView = findViewById(R.id.receiptRecyclerView);

        // Get data from intent
        String date = getIntent().getStringExtra("date");
        String sellerId = getIntent().getStringExtra("sellerId");
        double total = getIntent().getDoubleExtra("total", 0.0);
        ArrayList<String> products = getIntent().getStringArrayListExtra("products");
        double[] costs = getIntent().getDoubleArrayExtra("costs");
        int[] counts = getIntent().getIntArrayExtra("counts");

        // Set the texts
        dateText.setText("Date: " + date);
        sellerIdText.setText("Seller ID: " + sellerId);
        totalText.setText(String.format("Total: $%.2f", total));

        // Setup RecyclerView
        Map<String, List<ReceiptItem>> categoryItems = new HashMap<>();
        if (products != null && costs != null && counts != null) {
            for (int i = 0; i < products.size(); i++) {
                String category = getIntent().getStringArrayListExtra("categories").get(i);
                if (!categoryItems.containsKey(category)) {
                    categoryItems.put(category, new ArrayList<>());
                }
                categoryItems.get(category).add(new ReceiptItem(
                    products.get(i),
                    costs[i],
                    counts[i]
                ));
            }
        }

        List<ReceiptItem> receiptItems = new ArrayList<>();
        for (Map.Entry<String, List<ReceiptItem>> entry : categoryItems.entrySet()) {
            // Add category header
            receiptItems.add(new ReceiptItem(entry.getKey(), 0, 0, true));
            // Add items in that category
            receiptItems.addAll(entry.getValue());
        }

        ReceiptAdapter adapter = new ReceiptAdapter(receiptItems);
        receiptRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        receiptRecyclerView.setAdapter(adapter);
    }

    // Inner class for receipt items

    private static class ReceiptItem {
        String productName;
        double cost;
        int quantity;
        boolean isCategoryHeader;

        ReceiptItem(String productName, double cost, int quantity) {
            this.productName = productName;
            this.cost = cost;
            this.quantity = quantity;
            this.isCategoryHeader = false;
        }

        ReceiptItem(String productName, double cost, int quantity, boolean isCategoryHeader) {
            this.productName = productName;
            this.cost = cost;
            this.quantity = quantity;
            this.isCategoryHeader = isCategoryHeader;
        }
    }

    // Inner class for receipt adapter

    private static class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder> {
        private final List<ReceiptItem> items;

        ReceiptAdapter(List<ReceiptItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ReceiptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == 0) {  // Category header
                view = LayoutInflater.from(parent.getContext())
                        .inflate(android.R.layout.simple_list_item_1, parent, false);
                // Add some padding for category headers
                view.setPadding(30, 30, 30, 10);
            } else {  // Product item
                view = LayoutInflater.from(parent.getContext())
                        .inflate(android.R.layout.simple_list_item_1, parent, false);
                // Add some indentation for products
                view.setPadding(60, 10, 30, 10);
            }
            return new ReceiptViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReceiptViewHolder holder, int position) {
            ReceiptItem item = items.get(position);
            if (item.isCategoryHeader) {
                // If it's a category header, just show the category name
                holder.textView.setTextSize(18);  // Make category headers bigger
                holder.textView.setTypeface(null, Typeface.BOLD);  // Make them bold
                holder.textView.setText(item.productName);
            } else {
                // If it's a product, show the full details
                holder.textView.setTextSize(14);  // Reset text size
                holder.textView.setTypeface(null, Typeface.NORMAL);  // Reset to normal style
                holder.textView.setText(String.format("%s x%d @ $%.2f = $%.2f",
                        item.productName,
                        item.quantity,
                        item.cost,
                        item.cost * item.quantity));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).isCategoryHeader ? 0 : 1;
        }

        static class ReceiptViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            ReceiptViewHolder(View view) {
                super(view);
                textView = view.findViewById(android.R.id.text1);
            }
        }
    }
}