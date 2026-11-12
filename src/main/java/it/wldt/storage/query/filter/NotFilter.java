/*
 * Copyright (c) 2025 - Current Year
 * Marco Picone Ph.D
 * Email: picone.m@gmail.com
 * Website: https://www.marcopicone.net/
 * All rights reserved.
 *
 * This program is provided under a Dual Licensing model:
 * 1) GNU General Public License version 3.0 (GPL-3.0) for open-source, academic,
 *    research, non-profit, and other non-commercial use; or
 * 2) Commercial License, for any commercial use, proprietary development, or
 *    closed-source distribution. To obtain a Commercial License, please contact: Marco Picone (picone.m@gmail.com)
 *
 * By using this software, you agree to comply with the terms of the applicable license.
 * This applies to all forms of the software, including source code and compiled/binary forms.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package it.wldt.storage.query.filter;

/**
 * The child must not match.
 */
public final class NotFilter implements Filter {

    private final Filter child;

    public NotFilter(Filter child) {
        if (child == null)
            throw new IllegalArgumentException("Not filter child cannot be null");
        this.child = child;
    }

    public Filter getChild() {
        return child;
    }

    @Override
    public <R> R accept(FilterVisitor<R> visitor) {
        return visitor.visitNot(this);
    }

    @Override
    public String toString() {
        return "NOT(" + child + ")";
    }
}
