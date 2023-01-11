package com.kaiqkt.gateway.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class UnauthorisedException : ResponseStatusException(HttpStatus.UNAUTHORIZED)