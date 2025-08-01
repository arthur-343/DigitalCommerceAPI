package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.payload.AddressDTO;

import java.util.List;

public interface AddressesService {

    AddressDTO createAddress(AddressDTO address);

    List<AddressDTO> getAllAddress();

    AddressDTO getByIdAddress(Long addressId);

    List<AddressDTO> getAllAddressesByUser();

    AddressDTO updateAddress(Long addressId, AddressDTO addressDTO);

    String deletingAddress(Long addressId);

}
