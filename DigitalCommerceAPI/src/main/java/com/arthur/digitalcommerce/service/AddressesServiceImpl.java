package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.exceptions.APIException;
import com.arthur.digitalcommerce.exceptions.ResourceNotFoundException;
import com.arthur.digitalcommerce.model.Address;
import com.arthur.digitalcommerce.model.User;
import com.arthur.digitalcommerce.payload.AddressDTO;
import com.arthur.digitalcommerce.payload.APIResponse;
import com.arthur.digitalcommerce.payload.UserDTO;
import com.arthur.digitalcommerce.repository.AddressRepository;
import com.arthur.digitalcommerce.repository.UserRepository;
import com.arthur.digitalcommerce.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressesServiceImpl implements AddressesService {

    private static final Logger logger = LoggerFactory.getLogger(AddressesServiceImpl.class);

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    private void validateOwnership(Address address, String email) {
        if (!address.getUser().getEmail().equals(email)) {
            logger.warn("Unauthorized access attempt by {}", email);
            throw new APIException("Unauthorized access to this address");
        }
    }

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO) {
        User user = authUtil.loggedInUser();
        Address address = modelMapper.map(addressDTO, Address.class);
        address.setUser(user);
        List<Address> addressesList = user.getAddresses();
        addressesList.add(address);
        user.setAddresses(addressesList);
        Address savedAddress = addressRepository.save(address);
        return modelMapper.map(savedAddress, AddressDTO.class);
    }


    @Override
    public List<AddressDTO> getAllAddress() {
        List<Address> addresses = addressRepository.findAll();
        List<AddressDTO> dtos = new ArrayList<>();
        addresses.forEach(a -> dtos.add(modelMapper.map(a, AddressDTO.class)));

        logger.info("Retrieved {} addresses", dtos.size());
        return dtos;
    }

    @Override
    public AddressDTO getByIdAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        logger.info("Retrieved address {}", addressId);
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddressesByUser() {
        String email = authUtil.loggedInEmail();
        List<Address> addresses = addressRepository.findByUserEmail(email);

        List<AddressDTO> dtos = new ArrayList<>();
        addresses.forEach(a -> dtos.add(modelMapper.map(a, AddressDTO.class)));

        logger.info("Retrieved {} addresses for user {}", dtos.size(), email);
        return dtos;
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        Address addressFromDatabase = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        addressFromDatabase.setCity(addressDTO.getCity());
        addressFromDatabase.setCep(addressDTO.getCep());
        addressFromDatabase.setState(addressDTO.getState());
        addressFromDatabase.setCountry(addressDTO.getCountry());
        addressFromDatabase.setStreet(addressDTO.getStreet());
        addressFromDatabase.setBuildingName(addressDTO.getBuildingName());


        Address updatedAddress = addressRepository.save(addressFromDatabase);

        User user = addressFromDatabase.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        user.getAddresses().add(updatedAddress);
        userRepository.save(user);

        return modelMapper.map(updatedAddress, AddressDTO.class);
    }
    @Override
    public String deletingAddress(Long addressId) {
        String email = authUtil.loggedInEmail();
        Address address = addressRepository.findByIdAndUserEmail(addressId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        validateOwnership(address, email);
        addressRepository.deleteById(addressId);

        logger.info("Address deleted: {} by {}", addressId, email);
        return "Address deleted successfully.";
    }
}
