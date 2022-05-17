package com.home.tests.checkpoint1;

import lombok.Value;

@Value
    public  class Bid {
        Long id; // ID заявки
        Long participantId; // ID участника
        Long price; // предложенная цена
    }