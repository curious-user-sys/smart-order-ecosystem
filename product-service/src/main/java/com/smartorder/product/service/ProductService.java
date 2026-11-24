package com.smartorder.product.service;

import com.smartorder.product.dto.ProductRequest;
import com.smartorder.product.dto.ProductResponse;
import com.smartorder.product.entity.Product;
import com.smartorder.product.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.smartorder.product.exception.ResourceNotFoundException;
import com.smartorder.product.exception.InsufficientStockException;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    // Constructor injection: bağımlılıklar değişmez (final) ve zorunlu olur
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        // Request DTO'yu Entity'ye çeviriyoruz; id veritabanı tarafından üretilecek
        Product product = new Product(
                request.getName(),
                request.getPrice(),
                request.getStock()
        );
        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        // Tüm kayıtları çekip stream ile dış dünyaya uygun ProductResponse listesine çeviriyoruz
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = findProductOrThrow(id);
        return toResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        // Önce ürün var mı bakıyoruz; yoksa hata fırlatıyoruz
        Product product = findProductOrThrow(id);

        // Güncelleme kuralı: mevcut entity'nin alanlarını yeni istekle güncelliyoruz
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        // @Transactional aktif olduğu için değişiklikler commit sırasında veritabanına yansır
        Product updated = productRepository.save(product);
        return toResponse(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProductOrThrow(id);
        productRepository.delete(product);
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ürün bulunamadı. id=" + id));
    }

    // Dönüşüm mantığını tek bir private yardımcı metotta topladık (DRY prensibi)
    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock()
        );
    }


    @Transactional
    public ProductResponse decreaseStock(Long id, int quantity) {
        // İş kuralı 1: geçersiz adet isteğini erken reddediyoruz
        if (quantity <= 0) {
            throw new IllegalArgumentException("Düşülecek stok adedi 0'dan büyük olmalıdır");
        }

        // İş kuralı 2: ürün yoksa 404 üretecek ResourceNotFoundException fırlatır
        Product product = findProductOrThrow(id);

        // İş kuralı 3: stoğu eksiye düşürmeyi engelliyoruz
        if (product.getStock() < quantity) {
            throw new InsufficientStockException(
                    "Yetersiz stok. id=" + id + ", mevcut=" + product.getStock() + ", istenen=" + quantity
            );
        }

        product.setStock(product.getStock() - quantity);
        Product updated = productRepository.save(product);
        return toResponse(updated);
    }
    
    @Transactional
    public ProductResponse increaseStock(Long id, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Eklenecek stok adedi 0'dan büyük olmalıdır");
        }

        Product product = findProductOrThrow(id);
        product.setStock(product.getStock() + quantity);
        Product updated = productRepository.save(product);
        return toResponse(updated);
    }
}