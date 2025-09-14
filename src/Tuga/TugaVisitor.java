package Tuga;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link TugaParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface TugaVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link TugaParser#prog}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProg(TugaParser.ProgContext ctx);
	/**
	 * Visit a parse tree produced by {@link TugaParser#decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDecl(TugaParser.DeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link TugaParser#argdecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgdecl(TugaParser.ArgdeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link TugaParser#func}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc(TugaParser.FuncContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Write}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWrite(TugaParser.WriteContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Block}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(TugaParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by the {@code While}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhile(TugaParser.WhileContext ctx);
	/**
	 * Visit a parse tree produced by the {@code IfElse}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfElse(TugaParser.IfElseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Affect}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAffect(TugaParser.AffectContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Return}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturn(TugaParser.ReturnContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StatFunctionCall}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatFunctionCall(TugaParser.StatFunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Empty}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmpty(TugaParser.EmptyContext ctx);
	/**
	 * Visit a parse tree produced by {@link TugaParser#blck}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlck(TugaParser.BlckContext ctx);
	/**
	 * Visit a parse tree produced by the {@code LessGrtr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLessGrtr(TugaParser.LessGrtrContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Or}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr(TugaParser.OrContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AddSub}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddSub(TugaParser.AddSubContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Parens}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParens(TugaParser.ParensContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Var}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar(TugaParser.VarContext ctx);
	/**
	 * Visit a parse tree produced by the {@code EqDiff}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqDiff(TugaParser.EqDiffContext ctx);
	/**
	 * Visit a parse tree produced by the {@code UminusUnot}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUminusUnot(TugaParser.UminusUnotContext ctx);
	/**
	 * Visit a parse tree produced by the {@code String}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitString(TugaParser.StringContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Int}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInt(TugaParser.IntContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExprFunctionCall}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprFunctionCall(TugaParser.ExprFunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MultDivMod}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultDivMod(TugaParser.MultDivModContext ctx);
	/**
	 * Visit a parse tree produced by the {@code And}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnd(TugaParser.AndContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Real}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReal(TugaParser.RealContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Boolean}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoolean(TugaParser.BooleanContext ctx);
}