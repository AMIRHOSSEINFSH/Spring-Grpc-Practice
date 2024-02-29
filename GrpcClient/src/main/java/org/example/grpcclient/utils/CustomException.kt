package org.example.grpcclient.utils

class GrpcException(val eMessage: String,val e: Throwable? = null) : Exception(eMessage, e)