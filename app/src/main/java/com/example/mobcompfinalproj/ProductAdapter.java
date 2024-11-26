package com.example.mobcompfinalproj;

//general imports
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

//layout imports
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//other imports
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    
    private List<Product> products;
    private OnProductUpdateListener listener;

    public interface OnProductUpdateListener {
        void onQuantityChanged();
        void onProductRemoved(int position);
    }

    public ProductAdapter(List<Product> products, OnProductUpdateListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private TextView productNameText;
        private TextView priceText;
        private TextView quantityText;
        private Button decreaseButton;
        private Button increaseButton;
        private Button removeButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            productNameText = itemView.findViewById(R.id.productNameText);
            priceText = itemView.findViewById(R.id.priceText);
            quantityText = itemView.findViewById(R.id.quantityText);
            decreaseButton = itemView.findViewById(R.id.decreaseButton);
            increaseButton = itemView.findViewById(R.id.increaseButton);
            removeButton = itemView.findViewById(R.id.removeButton);
        }

        public void bind(Product product) {
            productNameText.setText(product.getProductName());
            priceText.setText(String.format("$%.2f", product.getPriceAsDouble()));
            quantityText.setText(String.valueOf(product.getSelectedQuantity()));
            decreaseButton.setOnClickListener(v -> {
                if (product.getSelectedQuantity() > 0) {
                    product.setSelectedQuantity(product.getSelectedQuantity() - 1);
                    quantityText.setText(String.valueOf(product.getSelectedQuantity()));
                    listener.onQuantityChanged();
                }
            });

            increaseButton.setOnClickListener(v -> {
                if (product.getSelectedQuantity() < product.getInventoryCount()) {
                    product.setSelectedQuantity(product.getSelectedQuantity() + 1);
                    quantityText.setText(String.valueOf(product.getSelectedQuantity()));
                    listener.onQuantityChanged();
                } else {
                    Toast.makeText(v.getContext(), 
                        "Cannot exceed available inventory", 
                        Toast.LENGTH_SHORT).show();
                }
            });

            removeButton.setOnClickListener(v -> {
                listener.onProductRemoved(getAdapterPosition());
            });
        }
    }
} 