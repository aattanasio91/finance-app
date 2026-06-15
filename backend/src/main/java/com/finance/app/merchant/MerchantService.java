package com.finance.app.merchant;

import com.finance.app.common.exception.BadRequestException;
import com.finance.app.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;

    public List<MerchantResponse> findAll() {
        return merchantRepository.findAll().stream()
                .map(MerchantResponse::from)
                .toList();
    }

    public MerchantResponse create(CreateMerchantRequest request) {
        String normalized = normalizeName(request.name());

        if (merchantRepository.existsByNormalizedName(normalized)) {
            throw new BadRequestException("Merchant already exists: " + request.name());
        }

        Merchant merchant = new Merchant(request.name(), normalized);
        merchant.setCategoryId(request.categoryId());
        merchant = merchantRepository.save(merchant);
        return MerchantResponse.from(merchant);
    }

    public MerchantResponse update(UUID id, UpdateMerchantRequest request) {
        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", "id", id));

        if (request.categoryId() != null) {
            merchant.setCategoryId(request.categoryId());
        }
        if (request.name() != null) {
            merchant.setName(request.name());
            merchant.setNormalizedName(normalizeName(request.name()));
        }

        merchant = merchantRepository.save(merchant);
        return MerchantResponse.from(merchant);
    }

    public static String normalizeName(String name) {
        return name.toUpperCase()
                .replaceAll("\\s+", " ")
                .replaceAll("[^A-Z0-9 ]", "")
                .trim();
    }
}
