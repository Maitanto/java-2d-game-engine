package engine.gal.aut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import engine.gal.actions.GALAction;
import engine.gal.actions.iGALAction;
import engine.gal.actions.Move;
import engine.gal.actions.Orient;
import engine.gal.actions.ProbabilisticAction;
import engine.gal.actions.Turn;
import engine.gal.actions.Get;
import engine.gal.actions.Hit;
import engine.gal.actions.Protect;
import engine.gal.actions.Explode;
import engine.gal.actions.Throw;
import engine.gal.actions.SequenceAction;
import engine.gal.actions.Dash;

import engine.gal.condition.GALCondition;
import engine.gal.condition.iGALCondition;
import engine.gal.condition.AtStep;
import engine.gal.condition.Closest;
import engine.gal.condition.Conjunction;
import engine.gal.condition.KeyCondition;
import engine.gal.condition.Struck;
import engine.gal.condition.Life;
import engine.gal.condition.MyDir;
import engine.gal.condition.Timer;

import engine.gal.arguments.Category;
import engine.gal.arguments.Direction;

import gal.ast.AST;
import gal.ast.Parameter;
import gal.ast.IntValue;

public class AST2Aut {
	private List<Automaton> list;

	public AST2Aut(AST ast) {
		this.list = new ArrayList<>();

		for (gal.ast.Automaton aut : ast.aut_list) {
			Map<String, State> stateMap = new HashMap<>();
			int stateIdCounter = 0;

			State initialEngineState = new State(aut.initial_state.name, stateIdCounter++);
			stateMap.put(aut.initial_state.name, initialEngineState);

			for (gal.ast.Mode md : aut.modes) {
				if (!stateMap.containsKey(md.state.name)) {
					stateMap.put(md.state.name, new State(md.state.name, stateIdCounter++));
				}
			}

			Automaton engineAuto = new Automaton(aut.name, initialEngineState);

			for (gal.ast.Mode md : aut.modes) {
				State sourceState = stateMap.get(md.state.name);

				for (gal.ast.Transition tr : md.behaviour.transitions) {
					State targetState = stateMap.get(tr.target.name);
					iGALCondition condition = convertCondition(tr.condition);
					iGALAction action = convertAction(tr.action);

					Transition engineTransition = new Transition(sourceState, condition, action, targetState);
					engineAuto.add(engineTransition);
				}
			}

			this.list.add(engineAuto);
		}
	}

	public List<Automaton> getAutomata() {
		return list;
	}

	// --- CONVERSION METHODS ---

	private iGALCondition convertCondition(gal.ast.Condition astCondition) {
		if (astCondition == null || astCondition.expression() == null) {
			return GALCondition.TRUE;
		}
		return convertExpression(astCondition.expression());
	}

	private iGALCondition convertExpression(gal.ast.Expression expr) {
		if (expr == null) {
			throw new IllegalArgumentException("GAL expression cannot be null");
		}

		// --- CASE 1: Standard Condition (FunCall) ---
		if (expr instanceof gal.ast.FunCall) {
			gal.ast.FunCall call = (gal.ast.FunCall) expr;
			String condName = call.name;

			if (condName == null) {
				throw new IllegalArgumentException("GAL condition name cannot be null");
			}

			switch (condName.toLowerCase()) {
			case "true":
				return GALCondition.TRUE;

			case "atstep":
				if (call.parameters.size() != 3) {
					throw new IllegalArgumentException(
							"AtStep condition requires 3 parameters: direction, category, step");
				}

				String dirParam = call.parameters.get(0).toString();
				String catParam = call.parameters.get(1).toString();

				Direction dir = Direction.canonical(dirParam);
				Category cat = Category.canonical(catParam);

				Parameter p2 = call.parameters.get(2);
				int step;
				if (p2 instanceof IntValue) {
					step = ((IntValue) p2).value;
				} else {
					step = Integer.parseInt(p2.toString());
				}

				return new AtStep(dir, cat, step);

			case "key":
				if (call.parameters.size() >= 1) {
					String keyName = call.parameters.get(0).toString();
					return new KeyCondition(keyName);
				}
				throw new IllegalArgumentException("Key condition requires 1 parameter");

			case "struck":
				if (call.parameters.size() >= 1) {
					Category category = Category.canonical(call.parameters.get(0).toString());
					return new Struck(category);
				}
				return new Struck();
			case "life":
				if (call.parameters.size() >= 1) {
					Parameter p = call.parameters.get(0);

					if (p instanceof IntValue) {
						return new Life(((IntValue) p).value);
					}

					return new Life(Integer.parseInt(p.toString()));
				}

				throw new IllegalArgumentException("Life condition requires 1 parameter");

			case "timer":
				return new Timer();
			case "mydir":
				if (call.parameters.size() >= 1) {
					Direction myDirParam = Direction.canonical(call.parameters.get(0).toString());
					return new MyDir(myDirParam);
				}
				throw new IllegalArgumentException("MyDir condition requires 1 parameter");
			case "closest":
				if (call.parameters.size() == 1) {
					Category closestCat = Category.canonical(call.parameters.get(0).toString());
					return new Closest(closestCat);
				}
				if (call.parameters.size() == 2) {
					Category closestCat = Category.canonical(call.parameters.get(0).toString());
					String p1 = call.parameters.get(1).toString();
					try {
						double radius = Double.parseDouble(p1);
						return new Closest(closestCat, radius);
					} catch (NumberFormatException e) {
						Direction closestDir = Direction.canonical(p1);
						return new Closest(closestCat, closestDir, -1);
					}
				}
				if (call.parameters.size() >= 3) {
					Category closestCat = Category.canonical(call.parameters.get(0).toString());
					String p1 = call.parameters.get(1).toString();
					String p21 = call.parameters.get(2).toString();
					try {
						Direction closestDir = Direction.canonical(p1);
						double radius = Double.parseDouble(p21);
						return new Closest(closestCat, closestDir, radius);
					} catch (Exception e) {
						throw new IllegalArgumentException("Closest(Cat, Dir, radius) : paramètres invalides");
					}
				}
				throw new IllegalArgumentException("Closest requires at least 1 parameter");
			default:
				throw new IllegalArgumentException("Unsupported GAL condition: " + condName);
			}
		}

		// --- CASE 2: Conjunction (BinaryOp) ---
		else if (expr instanceof gal.ast.BinaryOp) {
			gal.ast.BinaryOp binOp = (gal.ast.BinaryOp) expr;
			if ("&".equals(binOp.operator) || "and".equalsIgnoreCase(binOp.operator)) {
				Conjunction conjunction = new Conjunction();
				conjunction.add(convertExpression(binOp.left_operand));
				conjunction.add(convertExpression(binOp.right_operand));
				return conjunction;
			}
			throw new UnsupportedOperationException("Unsupported GAL binary operator: " + binOp.operator);
		}

		// --- CASE 3: Negation (UnaryOp) ---
		else if (expr instanceof gal.ast.UnaryOp) {
			gal.ast.UnaryOp unOp = (gal.ast.UnaryOp) expr;

			if ("!".equals(unOp.operator) || "not".equalsIgnoreCase(unOp.operator)) {
				throw new UnsupportedOperationException("GAL Not condition is not supported yet");
			}

			throw new UnsupportedOperationException("Unsupported GAL unary operator: " + unOp.operator);
		}

		throw new UnsupportedOperationException("Unsupported GAL expression type: " + expr.getClass().getName());
	}

	private iGALAction convertAction(gal.ast.Actions astAction) {
		if (astAction == null || astAction.actions.isEmpty()) {
			return GALAction.NOTHING;
		}

		List<gal.ast.FunCall> calls = astAction.actions;

		if (";".equals(astAction.operator)) {
			List<iGALAction> actions = new ArrayList<>();
			for (gal.ast.FunCall call : calls) {
				actions.add(convertSingleAction(call));
			}
			return actions.size() == 1 ? actions.get(0) : new SequenceAction(actions);
		}

		int explicitTotal = 0;
		int noPercentCount = 0;
		for (gal.ast.FunCall call : calls) {
			if (call.percent == gal.ast.FunCall.NO_PERCENT) {
				noPercentCount++;
			} else {
				explicitTotal += call.percent;
			}
		}

		int remaining = 100 - explicitTotal;
		int quota = (noPercentCount > 0) ? remaining / noPercentCount : 0;
		int leftover = (noPercentCount > 0) ? remaining % noPercentCount : 0;

		List<iGALAction> actions = new ArrayList<>();
		List<Integer> weights = new ArrayList<>();
		int leftoverGiven = 0;

		for (gal.ast.FunCall call : calls) {
			actions.add(convertSingleAction(call));
			if (call.percent == gal.ast.FunCall.NO_PERCENT) {
				int w = quota + (leftoverGiven < leftover ? 1 : 0);
				leftoverGiven++;
				weights.add(w);
			} else {
				weights.add(call.percent);
			}
		}

		if (actions.size() == 1) {
			return actions.get(0);
		}

		return new ProbabilisticAction(actions, weights);
	}

	private iGALAction convertSingleAction(gal.ast.FunCall call) {

		String actionName = call.name;

		if (actionName == null) {
			return GALAction.NOTHING;
		}

		switch (actionName.toLowerCase()) {
		case "move":
			if (call.parameters.size() >= 1) {
				try {
					String dirStr = call.parameters.get(0).toString();
					Direction dir = Direction.canonical(dirStr);

					if (call.parameters.size() >= 2) {
						Parameter p2 = call.parameters.get(1);
						if (p2 instanceof IntValue) {
							return new Move(dir, ((IntValue) p2).value);
						}
					}
					return new Move(dir, 1);
				} catch (IllegalArgumentException e) {
					throw e;
				}
			}
			return new Move();

		case "turn":
			if (call.parameters.size() >= 1) {
				Parameter p = call.parameters.get(0);
				if (p instanceof IntValue) {
					return new Turn(((IntValue) p).value);
				} else {
					try {
						Direction dir = Direction.canonical(p.toString());
						return new Turn(dir);
					} catch (IllegalArgumentException e) {
						throw e;
					}
				}
			}
			return new Turn(0);

		case "hit":
			if (call.parameters.size() >= 1) {
				Parameter p = call.parameters.get(0);

				if (p instanceof IntValue) {
					return new Hit(((IntValue) p).value);
				}

				return new Hit(Integer.parseInt(p.toString()));
			}

			return new Hit(1);
		case "rest":
			return GALAction.NOTHING;
		case "get":
			if (call.parameters.size() >= 1) {
				Category category = Category.canonical(call.parameters.get(0).toString());
				return new Get(category);
			}
			return new Get(null);
		case "protect":
			if (call.parameters.size() >= 1) {
				Parameter p = call.parameters.get(0);

				if (p instanceof IntValue) {
					return new Protect(((IntValue) p).value);
				}

				return new Protect(Double.parseDouble(p.toString()));
			}

			throw new IllegalArgumentException("Protect action requires 1 parameter");
		case "explode":
			return new Explode();
		case "throw":
			if (call.parameters.size() >= 1) {
				Direction direction = Direction.canonical(call.parameters.get(0).toString());
				return new Throw(direction);
			}

			return new Throw();
		case "orient":
			if (call.parameters.size() >= 1) {
				Direction dir = Direction.canonical(call.parameters.get(0).toString());
				return new Orient(dir);
			}
			throw new IllegalArgumentException("Orient requires 1 parameter");
		case "dash":
			if (call.parameters.size() >= 1) {
				Direction direction = Direction.canonical(call.parameters.get(0).toString());
				return new Dash(direction);
			}

			return new Dash();
		default:
			throw new IllegalArgumentException("Unsupported GAL action: " + actionName);
		}
	}
}
