package com.jetbrains.python.inspections;

import com.intellij.codeInsight.controlflow.ControlFlow;
import com.intellij.codeInsight.controlflow.ControlFlowUtil;
import com.intellij.codeInsight.controlflow.Instruction;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.Function;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.codeInsight.controlflow.ControlFlowCache;
import com.jetbrains.python.codeInsight.controlflow.ScopeOwner;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects unreachable code using control flow graph
 */
public class PyUnreachableCodeInspection extends PyInspection {
  @Nls
  @NotNull
  public String getDisplayName() {
    return PyBundle.message("INSP.NAME.unreachable.code");
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder,
                                        boolean isOnTheFly,
                                        @NotNull LocalInspectionToolSession session) {
    return new Visitor(holder, session);
  }

  public static class Visitor extends PyInspectionVisitor {
    public Visitor(@NotNull ProblemsHolder holder, @NotNull LocalInspectionToolSession session) {
      super(holder, session);
    }

    @Override
    public void visitElement(final PsiElement element) {
      if (element instanceof ScopeOwner) {
        final ControlFlow flow = ControlFlowCache.getControlFlow((ScopeOwner)element);
        final Instruction[] instructions = flow.getInstructions();
        final List<PsiElement> unreachable = new ArrayList<PsiElement>();
        if (instructions.length > 0) {
          ControlFlowUtil.iteratePrev(instructions.length - 1, instructions, new Function<Instruction, ControlFlowUtil.Operation>() {
            @Override
            public ControlFlowUtil.Operation fun(Instruction instruction) {
              if (instruction.allPred().isEmpty() && instruction.num() != 0) {
                unreachable.add(instruction.getElement());
              }
              return ControlFlowUtil.Operation.NEXT;
            }
          });
        }
        for (PsiElement e : unreachable) {
          registerProblem(e, PyBundle.message("INSP.unreachable.code"));
        }
      }
    }
  }
}
