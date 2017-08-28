/*
 * Copyright (c) 2017, Oracle and/or its affiliates.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.oracle.truffle.llvm.nodes.asm.support;

import java.math.BigInteger;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class LongDivision {
    public static class Result {
        private static final long INVALID = 0x8000000000000000L;

        public static final Result OVERFLOW = new Result(INVALID, INVALID);

        public final long quotient;
        public final long remainder;

        Result(long quotient, long remainder) {
            this.quotient = quotient;
            this.remainder = remainder;
        }

        public boolean invalid() {
            return quotient == INVALID && remainder == INVALID;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (!(o instanceof Result)) {
                return false;
            }
            Result r = (Result) o;
            return r.quotient == quotient && r.remainder == remainder;
        }

        @Override
        public int hashCode() {
            return (int) (quotient ^ remainder);
        }

        @Override
        public String toString() {
            return "Result[q=" + quotient + ",r=" + remainder + "]";
        }
    }

    @TruffleBoundary
    public static Result divu128by64(long a1, long a0, long b) {
        if (a1 == 0 && a0 > 0 && b > 0) {
            return new Result(a0 / b, a0 % b);
        }

        BigInteger x = u128(a1, a0);
        BigInteger y = u64(b);
        BigInteger[] result = x.divideAndRemainder(y);

        BigInteger q = result[0];
        BigInteger r = result[1];
        if (q.bitLength() > 64) {
            return Result.OVERFLOW;
        } else {
            return new Result(q.longValue(), r.longValue());
        }
    }

    @TruffleBoundary
    public static Result divs128by64(long a1, long a0, long b) {
        if (a1 == 0 && a0 > 0) {
            return new Result(a0 / b, a0 % b);
        }

        BigInteger x = s128(a1, a0);
        BigInteger y = BigInteger.valueOf(b);
        BigInteger[] result = x.divideAndRemainder(y);

        BigInteger q = result[0];
        BigInteger r = result[1];
        if (q.bitCount() > 64) {
            return Result.OVERFLOW;
        } else {
            return new Result(q.longValue(), r.longValue());
        }
    }

    private static BigInteger u64(long x) {
        byte[] bytes = new byte[]{
                        (byte) 0,
                        (byte) (x >> 56),
                        (byte) (x >> 48),
                        (byte) (x >> 40),
                        (byte) (x >> 32),
                        (byte) (x >> 24),
                        (byte) (x >> 16),
                        (byte) (x >> 8),
                        (byte) (x)
        };
        return new BigInteger(bytes);
    }

    private static BigInteger u128(long h, long l) {
        byte[] bytes = new byte[]{
                        (byte) 0,
                        (byte) (h >> 56),
                        (byte) (h >> 48),
                        (byte) (h >> 40),
                        (byte) (h >> 32),
                        (byte) (h >> 24),
                        (byte) (h >> 16),
                        (byte) (h >> 8),
                        (byte) (h),
                        (byte) (l >> 56),
                        (byte) (l >> 48),
                        (byte) (l >> 40),
                        (byte) (l >> 32),
                        (byte) (l >> 24),
                        (byte) (l >> 16),
                        (byte) (l >> 8),
                        (byte) (l)
        };
        return new BigInteger(bytes);
    }

    private static BigInteger s128(long h, long l) {
        byte[] bytes = new byte[]{
                        (byte) (h >> 56),
                        (byte) (h >> 48),
                        (byte) (h >> 40),
                        (byte) (h >> 32),
                        (byte) (h >> 24),
                        (byte) (h >> 16),
                        (byte) (h >> 8),
                        (byte) (h),
                        (byte) (l >> 56),
                        (byte) (l >> 48),
                        (byte) (l >> 40),
                        (byte) (l >> 32),
                        (byte) (l >> 24),
                        (byte) (l >> 16),
                        (byte) (l >> 8),
                        (byte) (l)
        };
        return new BigInteger(bytes);
    }
}
