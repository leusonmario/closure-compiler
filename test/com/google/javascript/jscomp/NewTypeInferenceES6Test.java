/*
 * Copyright 2015 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp;

import com.google.javascript.jscomp.CompilerOptions.LanguageMode;

/**
 * Tests for the new type inference on transpiled ES6 code.
 * We will eventually type check it natively, without transpiling.
 *
 * @author dimvar@google.com (Dimitris Vardoulakis)
 */

public final class NewTypeInferenceES6Test extends NewTypeInferenceTestBase {

  @Override
  protected void setUp() {
    super.setUp();
    compiler.getOptions().setLanguageIn(LanguageMode.ECMASCRIPT6);
    addES6TranspilationPasses();
  }

  public void testSimpleClasses() {
    typeCheck("class Foo {}");

    typeCheck(LINE_JOINER.join(
        "class Foo {}",
        "class Bar {}",
        "/** @type {!Foo} */ var x = new Bar;"),
        NewTypeInference.MISTYPED_ASSIGN_RHS);

    typeCheck(LINE_JOINER.join(
        "class Foo {",
        "  constructor(x) {",
        "    /** @type {string} */",
        "    this.x = x;",
        "  }",
        "}",
        "(new Foo('')).x - 5;"),
        NewTypeInference.INVALID_OPERAND_TYPE);
  }

  public void testClassInheritance() {
    typeCheck(LINE_JOINER.join(
        "class Foo {}",
        "class Bar extends Foo {}"));
  }

  public void testTaggedTemplateLitGlobalThisRef() {
    typeCheck("taggedTemp`${this.a}TaggedTemp`", NewTypeInference.GLOBAL_THIS);
  }

  public void testConstEmptyArrayNoWarning() {
    typeCheck("const x = [];");
  }

  public void testFunctionSubtypingContravariantReceiver() {
    typeCheck(LINE_JOINER.join(
        "class Foo {",
        "  method() {}",
        "}",
        "class Bar extends Foo {}",
        "function f(/** function(this:Bar) */ x) {}",
        "f(Foo.prototype.method);"));

    typeCheck(LINE_JOINER.join(
        "class Foo {",
        "  method() { return 123; }",
        "}",
        "class Bar extends Foo {}",
        "/**",
        " * @template T",
         " * @param {function(this:Bar):T} x",
        " */",
        "function f(x) {}",
        "f(Foo.prototype.method);"));
  }
}
