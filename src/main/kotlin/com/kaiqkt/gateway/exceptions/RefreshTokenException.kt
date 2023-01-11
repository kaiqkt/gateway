package com.kaiqkt.gateway.exceptions

import com.kaiqkt.gateway.entities.Error

class RefreshTokenException(val error: Error): Exception()